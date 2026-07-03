import csv
import os
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

CSV_FILE = os.path.join(ROOT, "reports", "infrastructure-data.csv")
OUTPUT_DIR = os.path.join(ROOT, "graphs")

os.makedirs(OUTPUT_DIR, exist_ok=True)

vus = []
throughput = []
avg_latency = []
p95_latency = []
failures = []

with open(CSV_FILE, newline="") as csvfile:
    reader = csv.DictReader(csvfile)

    for row in reader:
        vus.append(int(row["VirtualUsers"]))
        throughput.append(float(row["ThroughputRPS"]))
        avg_latency.append(float(row["AverageLatencyMs"]))
        p95_latency.append(float(row["P95LatencyMs"]))
        failures.append(float(row["HttpFailures"]))


def save_plot(filename):
    path = os.path.join(OUTPUT_DIR, filename)
    plt.tight_layout()
    plt.savefig(path, dpi=300)
    plt.close()
    print(f"Generated {path}")


############################################################
# Throughput
############################################################

plt.figure(figsize=(8,5))
plt.plot(vus, throughput, marker='o')
plt.grid(True)
plt.title("Infrastructure Benchmark - Throughput")
plt.xlabel("Virtual Users")
plt.ylabel("Requests / Second")

save_plot("throughput-vs-vus.png")

############################################################
# Average Latency
############################################################

plt.figure(figsize=(8,5))
plt.plot(vus, avg_latency, marker='o')
plt.grid(True)
plt.title("Infrastructure Benchmark - Average Latency")
plt.xlabel("Virtual Users")
plt.ylabel("Milliseconds")

save_plot("avg-latency-vs-vus.png")

############################################################
# P95 Latency
############################################################

plt.figure(figsize=(8,5))
plt.plot(vus, p95_latency, marker='o')
plt.grid(True)
plt.title("Infrastructure Benchmark - P95 Latency")
plt.xlabel("Virtual Users")
plt.ylabel("Milliseconds")

save_plot("p95-latency-vs-vus.png")

############################################################
# Scaling Efficiency
############################################################

baseline = throughput[0]

efficiency = []

for t, v in zip(throughput, vus):
    efficiency.append((t / baseline) / (v / vus[0]) * 100)

plt.figure(figsize=(8,5))
plt.plot(vus, efficiency, marker='o')
plt.grid(True)
plt.title("Infrastructure Benchmark - Scaling Efficiency")
plt.xlabel("Virtual Users")
plt.ylabel("Efficiency (%)")

save_plot("scaling-efficiency.png")

print()
print("Done.")