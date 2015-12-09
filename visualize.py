import matplotlib.pyplot as plt
from collections import defaultdict

def parse(path):
	utilizations = defaultdict(lambda: defaultdict(list))
	with open(path, "r") as f:
		for line in f:
			num_hosts_s, packet_size_s, utilization_s = line.split()		
			utilizations[int(packet_size_s)][int(num_hosts_s)].append(float(utilization_s))
	return utilizations

def unitlinspace(n):
	return [x / float(n) for x in range(n+1)]
	
def plot_data(data):
	color=iter(plt.cm.rainbow(unitlinspace(len(data))))

	for packet_size, dataset in sorted(data.items()):
		dataset = sorted(dataset.items())	
		keys, values = zip(*dataset)
		plt.plot(keys, values, label=packet_size, c=next(color))

	plt.legend(loc="center left", bbox_to_anchor=(1, 0.5))
	plt.show()
	
def plot_utilization():
	plt.xlim(0,30)
	plt.ylim(7,10)
	plt.xlabel("Number of hosts")
	plt.ylabel("Utilization")
	plot_data(parse("utilization_data.txt"))

	
def plot_sd():
	plt.xlim(0,30)
	plt.xlabel("Number of hosts")
	plt.ylabel("Standard deviation")
	plot_data(parse("standard_deviation_data.txt"))

from sys import argv
if len(argv) > 1 and argv[1] == "sd":
	print("plotting sd")
	plot_sd()
else:
	print("plotting utilization")
	plot_utilization()