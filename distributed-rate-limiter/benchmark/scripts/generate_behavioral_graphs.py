import os
import json
import csv
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

RESULTS_DIR = os.path.join(ROOT, "results", "behavioral")
REPORT_DIR = os.path.join(ROOT, "reports")
GRAPH_DIR = os.path.join(ROOT, "graphs", "behavioral")

os.makedirs(REPORT_DIR, exist_ok=True)
os.makedirs(GRAPH_DIR, exist_ok=True)

csv_file = os.path.join(REPORT_DIR, "behavioral-data.csv")

rows = []

############################################################
# Read all JSON benchmark summaries
############################################################

for algorithm in os.listdir(RESULTS_DIR):

    algorithm_dir = os.path.join(RESULTS_DIR, algorithm)

    if not os.path.isdir(algorithm_dir):
        continue

    for limit_dir in os.listdir(algorithm_dir):

        path = os.path.join(algorithm_dir, limit_dir)

        if not os.path.isdir(path):
            continue

        for file in os.listdir(path):

            if not file.endswith(".json"):
                continue

            with open(os.path.join(path, file), "r") as f:

                data = json.load(f)

            metrics = data["metrics"]

            vus = int(file.split("vu")[0])

            rows.append({

                "Algorithm": algorithm,

                "VirtualUsers": vus,

                "ThroughputRPS":
                    metrics["http_reqs"]["rate"],

                "AverageLatencyMs":
                    metrics["http_req_duration"]["avg"],

                "P95LatencyMs":
                    metrics["http_req_duration"]["p(95)"],

                "Requests":
                    metrics["http_reqs"]["count"],

                "HttpFailures":
                    metrics["http_req_failed"]["value"]

            })

############################################################
# Sort
############################################################

rows.sort(key=lambda r: (r["Algorithm"], r["VirtualUsers"]))

############################################################
# Write CSV
############################################################

with open(csv_file, "w", newline="") as csvfile:

    writer = csv.DictWriter(
        csvfile,
        fieldnames=[
            "Algorithm",
            "VirtualUsers",
            "ThroughputRPS",
            "AverageLatencyMs",
            "P95LatencyMs",
            "Requests",
            "HttpFailures"
        ]
    )

    writer.writeheader()

    writer.writerows(rows)

print("Generated:", csv_file)

############################################################
# Organize data
############################################################

algorithms = sorted(set(r["Algorithm"] for r in rows))
vus_levels = sorted(set(r["VirtualUsers"] for r in rows))

############################################################
# Helper
############################################################

def values(metric, vu):

    result = []

    for algorithm in algorithms:

        for row in rows:

            if row["Algorithm"] == algorithm and row["VirtualUsers"] == vu:

                result.append(row[metric])

    return result


def save(name):

    plt.tight_layout()

    plt.savefig(
        os.path.join(GRAPH_DIR, name),
        dpi=300
    )

    plt.close()

############################################################
# Throughput
############################################################

for vu in vus_levels:

    plt.figure(figsize=(8,5))

    plt.bar(
        algorithms,
        values("ThroughputRPS", vu)
    )

    plt.title(f"Throughput ({vu} VUs)")

    plt.ylabel("Requests / Second")

    plt.grid(axis="y")

    save(f"throughput-{vu}vus.png")

############################################################
# Average Latency
############################################################

for vu in vus_levels:

    plt.figure(figsize=(8,5))

    plt.bar(
        algorithms,
        values("AverageLatencyMs", vu)
    )

    plt.title(f"Average Latency ({vu} VUs)")

    plt.ylabel("Milliseconds")

    plt.grid(axis="y")

    save(f"avg-latency-{vu}vus.png")

############################################################
# P95 Latency
############################################################

for vu in vus_levels:

    plt.figure(figsize=(8,5))

    plt.bar(
        algorithms,
        values("P95LatencyMs", vu)
    )

    plt.title(f"P95 Latency ({vu} VUs)")

    plt.ylabel("Milliseconds")

    plt.grid(axis="y")

    save(f"p95-latency-{vu}vus.png")

############################################################
# Requests Processed
############################################################

for vu in vus_levels:

    plt.figure(figsize=(8,5))

    plt.bar(
        algorithms,
        values("Requests", vu)
    )

    plt.title(f"Requests Processed ({vu} VUs)")

    plt.ylabel("Requests")

    plt.grid(axis="y")

    save(f"requests-{vu}vus.png")

############################################################
# Failure Rate
############################################################

for vu in vus_levels:

    plt.figure(figsize=(8,5))

    plt.bar(
        algorithms,
        values("HttpFailures", vu)
    )

    plt.title(f"HTTP Failure Rate ({vu} VUs)")

    plt.ylabel("Failure Rate")

    plt.grid(axis="y")

    save(f"failures-{vu}vus.png")

print()
print("Behavioral graphs generated successfully.")