import json
import os
import matplotlib.pyplot as plt
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

############################################################
# Deployment
############################################################

if len(sys.argv) != 2:
    print("Usage:")
    print("    python generate_graphs.py <single-node|distributed>")
    sys.exit(1)

deployment = sys.argv[1]

if deployment not in ("single-node", "distributed"):
    print(f"Unsupported deployment: {deployment}")
    sys.exit(1)

RESULTS_DIR = os.path.join(
    ROOT,
    "results",
    deployment,
    "infrastructure"
)

OUTPUT_DIR = os.path.join(
    ROOT,
    "graphs",
    deployment,
    "infrastructure"
)

os.makedirs(OUTPUT_DIR, exist_ok=True)

rows = []

############################################################
# Read all benchmark summaries
############################################################

for folder in os.listdir(RESULTS_DIR):

    benchmark_dir = os.path.join(RESULTS_DIR, folder)

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

    vus = int(folder.replace("vu", ""))

    rows.append({

        "VirtualUsers": vus,

        "Throughput": metrics["http_reqs"]["rate"],

        "AverageLatency": metrics["http_req_duration"]["avg"],

        "P95Latency": metrics["http_req_duration"]["p(95)"],

        "FailureRate": metrics["http_req_failed"]["value"]

    })

############################################################
# Sort
############################################################

rows.sort(key=lambda r: r["VirtualUsers"])

vus = [r["VirtualUsers"] for r in rows]

throughput = [r["Throughput"] for r in rows]

avg_latency = [r["AverageLatency"] for r in rows]

p95_latency = [r["P95Latency"] for r in rows]

failure_rate = [r["FailureRate"] for r in rows]

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

plt.plot(vus, throughput, marker="o")

plt.grid(True)

plt.title("Infrastructure Benchmark - Throughput")

plt.xlabel("Virtual Users")

plt.ylabel("Requests / Second")

save("throughput-vs-vus.png")

############################################################
# Average Latency
############################################################

plt.figure(figsize=(8,5))

plt.plot(vus, avg_latency, marker="o")

plt.grid(True)

plt.title("Infrastructure Benchmark - Average Latency")

plt.xlabel("Virtual Users")

plt.ylabel("Milliseconds")

save("average-latency-vs-vus.png")

############################################################
# P95 Latency
############################################################

plt.figure(figsize=(8,5))

plt.plot(vus, p95_latency, marker="o")

plt.grid(True)

plt.title("Infrastructure Benchmark - P95 Latency")

plt.xlabel("Virtual Users")

plt.ylabel("Milliseconds")

save("p95-latency-vs-vus.png")

############################################################
# Failure Rate
############################################################

plt.figure(figsize=(8,5))

plt.plot(vus, failure_rate, marker="o")

plt.grid(True)

plt.title("Infrastructure Benchmark - HTTP Failure Rate")

plt.xlabel("Virtual Users")

plt.ylabel("Failure Rate")

save("failure-rate-vs-vus.png")

############################################################
# Scaling Efficiency
############################################################

baseline = throughput[0]

efficiency = []

for rps, vu in zip(throughput, vus):

    efficiency.append(
        (rps / baseline) /
        (vu / vus[0]) * 100
    )

plt.figure(figsize=(8,5))

plt.plot(vus, efficiency, marker="o")

plt.grid(True)

plt.title("Infrastructure Benchmark - Scaling Efficiency")

plt.xlabel("Virtual Users")

plt.ylabel("Efficiency (%)")

save("scaling-efficiency.png")

print()
print("Graphs generated successfully.")