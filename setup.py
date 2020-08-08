import pip
import sys
import struct;

print("bits: " + str(struct.calcsize("P") * 8))
print(sys.version)
try:
    from pip import main as pip
except:
    from pip._internal.main import main as pip
# pip(['install', '--user','tensorflow'])
# pip(['install', '--user','--upgrade','pip'])
pip(['install', '--user','ffmpeg'])
pip(['install', '--user','spleeter'])
pip(['show', 'spleeter'])
