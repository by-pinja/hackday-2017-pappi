import random, datetime

timestamp = datetime.datetime.now()
f = open('dataset_4.csv', 'w')

# f.write('sep=,\n')
f.write('label,temp,timestamp\n')

temp = 20

for z in range(2000): 
	temp += .03
	timestamp = timestamp + datetime.timedelta(seconds=60)
	
	rand1 = random.random()
	poikkeama = 0
	label = 1
	
	if (rand1 < .01):
		label = 2
		poikkeama = 50
		rand2 = random.random()
		if (rand2 > .5):
			poikkeama *= -1
		
	this_temp = temp
	if (z > 800 and z < 1100):
		this_temp -= 40
		
	f.write(str(label) + ',' + str(this_temp + ((6 * random.random()) - 3) + poikkeama) + ',' + str(timestamp) + '\n')  # python will convert \n to os.linesep
		
f.close()  # you can omit in most cases as the destructor will call it