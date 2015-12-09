import matplotlib.pyplot as plt
from collections import defaultdict

numHost = range(31)[1:]
utilizations = defaultdict(lambda: defaultdict(list))
		
with open("utilization_data.txt", "r") as f:
	for line in f:
		num_hosts_s, packet_size_s, utilization_s = line.split()		
		utilizations[int(packet_size_s)][int(num_hosts_s)].append(float(utilization_s))

#Plot graphs
plt.xlim(0,30)
plt.ylim(7,10)
plt.xlabel("Number of hosts")
plt.ylabel("utilization")

#for key in utilizations:
#    print key, utilizations[key], len(utilizations[key])

from pprint import pprint
pprint(utilizations)

for packet_size, dataset in utilizations.iteritems():
	dataset = sorted(dataset.items(), key = lambda (k, v): k)	
	keys, values = zip(*dataset)
	
	plt.plot(keys, values, label=packet_size)

#plt.legend()
plt.show()
