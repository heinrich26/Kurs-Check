from decimal import Decimal
import itertools
import re
from typing import Iterable, Iterator


test_svg = 'M 14,0 H 2 C 0.89,0 0,0.9 0,2 v 14 c 0,1.1 0.89,2 2,2 h 14 c 1.1,0 2,-0.9 2,-2 V 4 Z M 9,16 c -1.66,0 -3,-1.34 -3,-3 0,-1.66 1.34,-3 3,-3 1.66,0 3,1.34 3,3 0,1.66 -1.34,3 -3,3 z M 12,6 H 2 V 2 h 10 z'
svg = input("SVG-Element (aus der .svg Datei): ") or test_svg
scale = Decimal(input("Scale: ") or 1)
print('\n')


def gen(path: str) -> Iterator[str]:
    path = path.replace("-", " -")
    cur = path[0]

    dot = False
    for c in path[1:]:
        if c.isalpha():
            yield cur
            cur = c
            dot = False
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
start_pos = (0, 0)


def add_call(name, coords: Iterable[Decimal], size: int = None) -> str:
    if (isinstance(coords, Iterator)):
        coords = tuple(coords)


    if (size != None and size != len(coords)):
        for i in range(0, len(coords), size):
            add_call(name, coords[i:i + size])
    else:
        calls.append(f"{name}({', '.join(str(float(round(num, 2))) for num in coords)})")
        global pos
        pos = coords[-2:]


def make_tuples(n, iterable):
    iterator = iter(iterable)

    return tuple(itertools.takewhile(bool, (tuple(itertools.islice(iterator, n)) for _ in itertools.repeat(None))))

def make_abs(coords: Iterable[Decimal]) -> Iterable[Decimal]:
    x, y = pos
    _coords = []
    for dx, dy in make_tuples(2, coords):
        _coords += (dx + x, dy + y)
        x += dx
        y += dy
    return _coords


def make_abs(coords: Iterable[Decimal], block_size: int) -> tuple[Decimal]:
    x, y = pos
    _coords = []
    for xs in make_tuples(block_size, coords):
        _coords += ((dx + x, dy + y) for dx, dy in make_tuples(2, xs))
        x += xs[-2]
        y += xs[-1]
    return tuple(itertools.chain(*_coords))


def reflect(point: tuple[Decimal, Decimal], origin: tuple[Decimal, Decimal]) -> tuple[Decimal, Decimal]:
    p_x, p_y = point
    o_x, o_y = origin
    return (2 * o_x - p_x, 2 * o_y - p_y)


prev_instruction = None
prev_handle = []
for command in gen(svg):
    instruction = command[0]
    coords = [Decimal(x) * scale for x in re.split('[ ,]', command[1:]) if x != '']

    if (instruction in 'ml'):
        coords = tuple(make_abs(coords))

    match instruction:
        case 'M' | 'm':
            add_call('moveTo', coords[:2])
            # Startposition des Pfads merken
            start_pos = pos

            if len(coords) > 2:
                add_call('lineTo', coords[2:], 2)
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
        case 'C':
            add_call('curveTo', coords, 6)
        case 'c':
            coords = make_abs(coords, 6)
            add_call('curveTo', coords, 6)
        case 'Q':
            add_call('quadTo', coords, 4)
        case 'q':
            coords = make_abs(coords, 4)
            add_call('quadTo', coords, 4)
        case 'z' | 'Z':
            calls.append('closePath()')
            pos = start_pos
        case 'S':
            handle = reflect(prev_handle, pos) if (prev_instruction == 'c' or prev_instruction == 'q') else pos
            for call in make_tuples(4, coords):
                add_call('curveTo', handle + call)
                handle = call[-4:-2]
        case 's':
            coords = make_abs(coords, 4)
            handle = reflect(prev_handle, pos) if (prev_instruction == 'c' or prev_instruction == 'q') else pos
            for call in make_tuples(4, coords):
                add_call('curveTo', handle + call)
                handle = call[-4:-2]
        case _:
            print(command[0])
            print("error")

    prev_instruction = instruction.lower()
    prev_handle = coords[-4:-2]


sep = '\n\t'
print(f"GeneralPath().apply \u007b\n\t{sep.join(calls)}\n\u007d")
sep = ';\n\tpath.'
print(f"GeneralPath path = new GeneralPath();\n\u007b\n\tpath.{sep.join(calls)};\n\u007d")
