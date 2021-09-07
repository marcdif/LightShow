from handlers.color import Color
from handlers.lightstrip import *
from argparse import ArgumentParser

parser = ArgumentParser()
parser.add_argument("-r", dest="red", help="Red", required=True)
parser.add_argument("-g", dest="green", help="Green", required=True)
parser.add_argument("-b", dest="blue", help="Blue", required=True)

args = parser.parse_args()

set_full_strip(Color(int(args.red), int(args.green), int(args.blue)))
