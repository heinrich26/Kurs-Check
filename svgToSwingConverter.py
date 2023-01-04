from decimal import Decimal
import itertools
import re
from typing import Iterable, Iterator


svg = input("SVG-Path: ")
scale = Decimal(input("Scale: ") or 1)
print('\n')
# svg = 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-4h2v2h-2zm1.61-9.96c-2.06-.3-3.88.97-4.43 2.79-.18.58.26 1.17.87 1.17h.2c.41 0 .74-.29.88-.67.32-.89 1.27-1.5 2.3-1.28.95.2 1.65 1.13 1.57 2.1-.1 1.34-1.62 1.63-2.45 2.88 0 .01-.01.01-.01.02-.01.02-.02.03-.03.05-.09.15-.18.32-.25.5-.01.03-.03.05-.04.08-.01.02-.01.04-.02.07-.12.34-.2.75-.2 1.25h2c0-.42.11-.77.28-1.07.02-.03.03-.06.05-.09.08-.14.18-.27.28-.39.01-.01.02-.03.03-.04.1-.12.21-.23.33-.34.96-.91 2.26-1.65 1.99-3.56-.24-1.74-1.61-3.21-3.35-3.47z'


def gen(path: str) -> Iterator[str]:
    path = path.replace("-", " -")
    cur = path[0]

    dot = False
    for c in path[1:]:
        if c.isalpha():
            yield cur
            cur = c
        else:
            if (c == "."):
                if (dot):
                    cur += " 0"
                dot = True
            elif (dot and (c == " " or c == ",")):
                dot = False
            cur += c
    yield cur


calls: list[str] = []
global pos
pos = (0, 0)


def add_call(name, coords: Iterable[Decimal], size: int = None) -> str:
    if (isinstance(coords, Iterator)):
        coords = tuple(coords)


    if (size != None and size != len(coords)):
        for i in range(0, len(coords), size):
            add_call(name, coords[i:i + size])
    else:
        calls.append(f"{name}({', '.join(f'{num:.1f}' for num in coords)})")
        global pos
        pos = coords[-2:]


def make_tuples(n, iterable):
    iterator = iter(iterable)
    
    return tuple(itertools.takewhile(bool, (tuple(itertools.islice(iterator, n)) for _ in itertools.repeat(None))))

def make_abs(coords: Iterable[Decimal]) -> Iterable[Decimal]:
    pos_x, pos_y = pos
    return itertools.chain(*[(x + pos_x, y + pos_y) for x, y in make_tuples(2, coords)])


def reflect(point: tuple[Decimal, Decimal], origin: tuple[Decimal, Decimal]) -> tuple[Decimal, Decimal]:
    p_x, p_y = point
    o_x, o_y = origin
    return (2 * o_x - p_x, 2* o_y - p_y)


prev_instruction = None
prev_handle = []
for command in gen(svg):
    instruction = command[0]
    coords = [Decimal(x) * scale for x in re.split('[ ,]', command[1:]) if x != '']


    if (instruction != 'z' and instruction.islower() and len(coords) % 2 == 0):
        coords = tuple(make_abs(coords))

    match instruction:
        case 'M' | 'm':
            add_call('moveTo', coords)
        case 'L' | 'l':
            add_call('lineTo', coords, 2)
        case 'H':
            add_call('lineTo', (coords[0], pos[1]))
        case 'h':
            add_call('lineTo', (coords[0] + pos[0], pos[1]))
        case 'V':
            add_call('lineTo', (pos[0], coords[0]))
        case 'v':
            add_call('lineTo', (pos[0], coords[0] + pos[1]))
        case 'C' | 'c':
            add_call('curveTo', coords, 6)
        case 'Q' | 'q':
            add_call('quadTo', coords, 4)
        case 'z' | 'Z':
            calls.append('closePath()')
        case 'S' | 's':
            handle = reflect(prev_handle, pos) if (prev_instruction == 'c' or prev_instruction == 'c') else pos
            for call in make_tuples(4, coords):
                add_call('curveTo', handle + call)
                handle = call[-4:-2]
        case _:
            print(command[0])
            print("error")

    prev_instruction = instruction.lower()
    prev_handle = coords[-4:-2]


nt = '\n\t'
print(f"GeneralPath().apply \u007b\n\t{nt.join(calls)}\n\u007d")
