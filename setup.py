import pip
import sys
import struct;

print("bits: " + str(struct.calcsize("P") * 8))
print(sys.version)
try:
    from pip import main as pip
except:
    from pip._internal.main import main as pip
pip(['install', '--user','spotdl'])
pip(['install', '--user','ffmpeg'])
pip(['install', '--user','spleeter'])

# pip(['install', '--user','--upgrade','--force-reinstall','spotdl'])
# pip(['install', '--user','--upgrade','--force-reinstall','ffmpeg'])
# pip(['install', '--user','--upgrade','--force-reinstall','spleeter'])

# pip(['show', 'spleeter'])
