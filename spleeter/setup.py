import pip
import sys
import struct;

print("bits: " + str(struct.calcsize("P") * 8))
print(sys.version)
try:
    from pip import main as pip
except:
    from pip._internal.main import main as pip
pip(['install',  'tensorflow'])
pip(['install',  'ffmpeg'])
pip(['install', 'spleeter'])
pip(['show','spleeter'])
