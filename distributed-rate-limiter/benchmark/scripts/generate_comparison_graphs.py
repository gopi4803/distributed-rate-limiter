import json
import os
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

RESULTS_ROOT = os.path.join(ROOT, "results")

OUTPUT_DIR = os.path.join(
    ROOT,
    "graphs",
    "comparison",
    "infrastructure"
)

os.makedirs(OUTPUT_DIR, exist_ok=True)


############################################################
# Read infrastructure benchmark summaries
############################################################

def load_results(deployment):

    infrastructure_dir = os.path.join(
        RESULTS_ROOT,
        deployment,
        "infrastructure"
    )

    rows = []

    for folder in os.listdir(infrastructure_dir):

        benchmark_dir = os.path.join(
            infrastructure_dir,
            folder
        )

        if not os.path.isdir(benchmark_dir):
            continue

        summary_file = os.path.join(
            benchmark_dir,
            f"{folder}-summary.json"
        )

        if not os.path.exists(summary_file):
            continue

        with open(summary_file, "r") as f:
            data = json.load(f)

        metrics = data["metrics"]

        vu = int(folder.replace("vu", ""))

        rows.append({

            "VirtualUsers": vu,

            "Throughput": metrics["http_reqs"]["rate"],

            "AverageLatency": metrics["http_req_duration"]["avg"],

            "P95Latency": metrics["http_req_duration"]["p(95)"],

            "FailureRate": metrics["http_req_failed"]["value"]

        })

    rows.sort(key=lambda r: r["VirtualUsers"])

    return rows


single = load_results("single-node")
distributed = load_results("distributed")


############################################################
# Helper
############################################################

def save(name):

    plt.tight_layout()

    plt.savefig(
        os.path.join(OUTPUT_DIR, name),
        dpi=300
    )

    plt.close()


############################################################
# Throughput
############################################################

plt.figure(figsize=(8,5))

plt.plot(
    [r["VirtualUsers"] for r in single],
    [r["Throughput"] for r in single],
    marker="o",
    label="Single Node"
)

plt.plot(
    [r["VirtualUsers"] for r in distributed],
    [r["Throughput"] for r in distributed],
    marker="o",
    label="Distributed"
)

plt.grid(True)

plt.legend()

plt.title("Infrastructure Throughput Comparison")

plt.xlabel("Virtual Users")

plt.ylabel("Requests / Second")

save("throughput-comparison.png")


############################################################
# Average Latency
############################################################

plt.figure(figsize=(8,5))

plt.plot(
    [r["VirtualUsers"] for r in single],
    [r["AverageLatency"] for r in single],
    marker="o",
    label="Single Node"
)

plt.plot(
    [r["VirtualUsers"] for r in distributed],
    [r["AverageLatency"] for r in distributed],
    marker="o",
    label="Distributed"
)

plt.grid(True)

plt.legend()

plt.title("Average Latency Comparison")

plt.xlabel("Virtual Users")

plt.ylabel("Milliseconds")

save("average-latency-comparison.png")


############################################################
# P95 Latency
############################################################

plt.figure(figsize=(8,5))

plt.plot(
    [r["VirtualUsers"] for r in single],
    [r["P95Latency"] for r in single],
    marker="o",
    label="Single Node"
)

plt.plot(
    [r["VirtualUsers"] for r in distributed],
    [r["P95Latency"] for r in distributed],
    marker="o",
    label="Distributed"
)

plt.grid(True)

plt.legend()

plt.title("P95 Latency Comparison")

plt.xlabel("Virtual Users")

plt.ylabel("Milliseconds")

save("p95-latency-comparison.png")


############################################################
# Scaling Efficiency
############################################################

def scaling(rows):

    baseline = rows[0]["Throughput"]

    efficiencies = []

    for row in rows:

        efficiencies.append(

            (row["Throughput"] / baseline) /

            (row["VirtualUsers"] / rows[0]["VirtualUsers"]) * 100

        )

    return efficiencies


plt.figure(figsize=(8,5))

plt.plot(
    [r["VirtualUsers"] for r in single],
    scaling(single),
    marker="o",
    label="Single Node"
)

plt.plot(
    [r["VirtualUsers"] for r in distributed],
    scaling(distributed),
    marker="o",
    label="Distributed"
)

plt.grid(True)

plt.legend()

plt.title("Scaling Efficiency Comparison")

plt.xlabel("Virtual Users")

plt.ylabel("Efficiency (%)")

save("scaling-efficiency-comparison.png")

print()
print("Comparison graphs generated successfully.")