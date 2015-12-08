import matplotlib.pyplot as plt

numHost = range(31)[1:]
utilizations = { '64' :  [],
                 '128':  [],
                 '256':  [],
                 '512':  [],
                 '768':  [],
                 '1536': [],
                 '3072': [],
                }

packet_sizes = [64, 128, 256, 512, 768, 1536, 3072]

for packet_size in packet_sizes:
    with open("utilization_data_{}.txt".format(packet_size),"r") as f:
        for line in f:
            utilization = float(line)
            utilizations[str(packet_size)].append(utilization)

#Plot graphs
plt.xlim(0,30)
plt.ylim(0,10)
plt.xlabel("Number of hosts")
plt.ylabel("utilization")

#for key in utilizations:
#    print key, utilizations[key], len(utilizations[key])

for packet_size in utilizations:
    plt.plot(numHost, utilizations[packet_size], label=packet_size)

#plt.legend()
plt.show()
