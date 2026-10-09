import math, zlib, struct, os

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources', 'assets', 'sharingan', 'textures')


def png(path, w, h, px):
    raw = b''.join(b'\x00' + bytes(c for p in px[y * w:(y + 1) * w] for c in p) for y in range(h))

    def chunk(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)

    data = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)) \
        + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')
    with open(path, 'wb') as f:
        f.write(data)


# (numero de pontas, giro, largura) de cada Mangekyou
PAT = {'itachi': (3, 2.4, 0.42), 'sasuke': (3, 0.8, 0.55), 'obito': (3, 5.0, 0.36),
       'shisui': (4, -3.0, 0.30), 'eterno': (6, 1.6, 0.26)}


def iris(kind, u, v):
    r = math.hypot(u, v)
    if r > 1:
        return None
    black = r > 0.93 or r < 0.17
    phi = math.atan2(v, u)
    if kind in ('1', '2', '3'):
        n = int(kind)
        if abs(r - 0.58) < 0.025:
            black = True
        for k in range(n):
            a = k * 2 * math.pi / n - math.pi / 2
            if math.hypot(u - 0.58 * math.cos(a), v - 0.58 * math.sin(a)) < 0.14:
                black = True
            for s in range(1, 7):
                aa = a + s * 0.11
                if math.hypot(u - 0.58 * math.cos(aa), v - 0.58 * math.sin(aa)) < 0.14 * (1 - s / 8):
                    black = True
    else:
        n, c, w = PAT[kind]
        t = (r - 0.17) / 0.76
        if 0 <= t <= 1:
            for k in range(n):
                center = k * 2 * math.pi / n + c * t
                d = (phi - center + math.pi) % (2 * math.pi) - math.pi
                if abs(d) < w * (1 - t) + 0.04:
                    black = True
        if abs(r - 0.88) < 0.02:
            black = True
    if black:
        return (12, 8, 8, 255)
    s = 1 - 0.35 * r
    return (int(215 * s + 20), 14, 16, 255)


def render(size, fn, ss=3):
    px = []
    for y in range(size):
        for x in range(size):
            acc = [0, 0, 0, 0]
            for sy in range(ss):
                for sx in range(ss):
                    c = fn((x + (sx + .5) / ss) / size, (y + (sy + .5) / ss) / size)
                    if c:
                        acc[0] += c[0] * c[3]
                        acc[1] += c[1] * c[3]
                        acc[2] += c[2] * c[3]
                        acc[3] += c[3]
            n = ss * ss
            if acc[3] == 0:
                px.append((0, 0, 0, 0))
            else:
                px.append((int(acc[0] / acc[3]), int(acc[1] / acc[3]), int(acc[2] / acc[3]), int(acc[3] / n)))
    return px


def gui(kind):
    return lambda x, y: iris(kind, (x - .5) / .48, (y - .5) / .48)


def item(kind):
    def f(x, y):
        dx, dy = x - .5, y - .5
        r = math.hypot(dx, dy)
        if r > .5:
            return None
        if r > .44:
            return (90, 20, 20, 255)
        ir = iris(kind, dx / .3, dy / .3)
        if ir:
            return ir
        return (235, 222, 222, 255)
    return f


def effect(x, y):
    dx, dy = x - .5, y - .5
    r = math.hypot(dx, dy)
    if r > .48:
        return None
    flame = abs(dx) < .3 * (1 - (.5 - dy)) + .05 and dy > -.3
    if r > .4:
        return (60, 20, 100, 255)
    return (20, 8, 40, 255) if not flame else (110, 40, 190, 255)


for d in ('gui', 'item', 'mob_effect'):
    os.makedirs(os.path.join(OUT, d), exist_ok=True)
for k in ['1', '2', '3', 'itachi', 'sasuke', 'obito', 'shisui', 'eterno']:
    png(os.path.join(OUT, 'gui', 'eye_%s.png' % k), 64, 64, render(64, gui(k)))
for k in ['itachi', 'sasuke', 'obito', 'shisui']:
    png(os.path.join(OUT, 'item', 'eye_%s.png' % k), 16, 16, render(16, item(k), 4))
png(os.path.join(OUT, 'mob_effect', 'amaterasu.png'), 18, 18, render(18, effect, 4))
print('ok')
