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
	
def plot_utilization():
	print("plotting utilization")
	plt.title("Utilization")
	plt.xlim(0,30)
	#plt.ylim(7,10)
	plt.xlabel("Number of Hosts")
	plt.ylabel("Utilization (Mb/s)")
	plot_data(parse("utilization_data.txt"))
	plt.savefig("util.png")

	
def plot_sd():
	print("plotting sd")
	plt.title("Standard Deviation of Utilization")
	plt.xlim(0,30)
	plt.xlabel("Number of Hosts")
	plt.ylabel("Standard Deviation of Utilization (Mb/s)")
	plot_data(parse("standard_deviation_data.txt"))
	
def plot_packet_rate():
	print("plotting packet rate")
	plt.title("Packet Rate")
	plt.xlim(0,30)
	plt.xlabel("Number of Hosts")
	plt.ylabel("Packet Rate (packets/s)")
	plot_data(parse("packet_data.txt"))
	
def plot_delay():
	print("plotting transmission delay")
	plt.title("Transmission Delay")
	plt.xlim(0,30)
	plt.xlabel("Number of Hosts")
	plt.ylabel("Average of Host Transmission Delay Averages (ms)")
	plot_data(parse("delay_data.txt"))

from sys import argv
if len(argv) > 1:
	if argv[1] == "sd":
		plot_sd()
	elif argv[1] == "packet":
		plot_packet_rate()
	elif argv[1] == "delay":
		plot_delay()
	else:
		plot_utilization()	

	plt.legend(loc="center left", bbox_to_anchor=(1, 0.5))
	plt.show()
else:
	#plt.figure(num=None, figsize=(8, 6), dpi=100, facecolor='w', edgecolor='k')
	plt.subplot(2, 2, 1)
	plot_utilization()
	plt.subplot(2, 2, 2)
	plot_sd()
	plt.subplot(2, 2, 3)
	plot_packet_rate()
	plt.subplot(2, 2, 4)
	plot_delay()
	
	plt.legend(loc="center left", bbox_to_anchor=(1, 0.5))
	plt.show()