import urllib.request
import json
import random
import time, datetime
import threading
from collections import deque
import paho_mqtt

url = 'https://ussouthcentral.services.azureml.net/workspaces/8854f731dde4458bb29d0138178c472e/services/75278a1d64fe4c89ad4d767c4d076e19/execute?api-version=2.0&format=swagger'
api_key = 'jdjJDeLioPMJlY9njc+DmNT6x1+B043kxGWAiDGHT13H5dIgSlks/Lb2nlM2Tmu7pg/xrhGC3ympezVCkqebNQ=='
headers = {'Content-Type':'application/json', 'Authorization':('Bearer '+ api_key)}

temp = 35

def process():
	global temp
	
	last_error_ind = -99999
	treshold = 50
	
	d = deque()
	ind = 0
	while True:
		ind = ind + 1
		
		stamp = str(datetime.datetime.now())
		temp_str = '%.1f' % (temp + (random.random() - .5))
		
		item = {
			'label': "1",   
			'temp': temp_str,
			'timestamp': stamp,   
		}
	
		d.append(item)
		
		data = {
			"Inputs": {
				"input1": list(d),
			},
			"GlobalParameters":  {
			}
		}
		
		print('Current temperature: ' + temp_str)
		
		if (len(d) >= 50):		
			d.popleft()
			
		# print('Send req ' + str(ind) + ' ' + stamp)
		body = str.encode(json.dumps(data))
		req = urllib.request.Request(url, body, headers)
		response = urllib.request.urlopen(req)
		result = response.read()
		data = json.loads(result.decode('utf-8'))
		
		for result in data['Results']['output1']:
			if (result['Alert indicator'] != '0'):				
				if (ind > (last_error_ind + treshold)):
					print(result)
					last_error_ind = ind
					paho_mqtt.send_notification()
					# print(json.dumps(data))
					
		# time.sleep(.5)

try:
	thread = threading.Thread(target=process)
	thread.daemon = True
	thread.start()
	while True:
		exit_signal = input('Type "a" anytime to cause anomaly\n')
		if exit_signal == 'a':
			temp += 18 if (random.random() > .5) else -18
		
except Exception as error:
    print("The request failed" + str(error))
