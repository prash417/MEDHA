"""Generate glossy HUD chips and circular buttons for the landscape lobby."""
from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter

OUT = Path(r"C:\Users\kagep\Projects\MEDHA\app\src\main\res\drawable-nodpi")


def lerp(a, b, t):
    return int(a + (b - a) * t)


def mix(c0, c1, t):
    return tuple(lerp(c0[i], c1[i], t) for i in range(4))


def radial(size, cx, cy, radius, stops):
    """stops: list of (t, rgba) from 0..1."""
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = im.load()
    for y in range(size):
        dy = y - cy
        for x in range(size):
            d = math.hypot(x - cx, y - dy * 0 + (y - cy)) / radius
            if d >= 1:
                continue
            for i in range(len(stops) - 1):
                t0, c0 = stops[i]
                t1, c1 = stops[i + 1]
                if t0 <= d <= t1:
                    local = 0 if t1 == t0 else (d - t0) / (t1 - t0)
                    px[x, y] = mix(c0, c1, local)
                    break
    return im


def circle(d, xy, r, fill, outline=None, width=1):
    x, y = xy
    d.ellipse((x - r, y - r, x + r, y + r), fill=fill, outline=outline, width=width)


def glow_layer(size, color, blur, shape="circle"):
    g = Image.new("RGBA", size, (0, 0, 0, 0))
    d = ImageDraw.Draw(g)
    pad = blur * 2
    w, h = size
    if shape == "circle":
        d.ellipse((pad, pad, w - pad, h - pad), fill=color)
    else:
        d.rounded_rectangle((pad, pad, w - pad, h - pad), radius=(h - 2 * pad) / 2, fill=color)
    return g.filter(ImageFilter.GaussianBlur(blur))


def glass_panel(width, height, rim, glow, radius=None, pad=None, fill=(8, 14, 24, 110), gloss=(255, 255, 255, 22), rim_width=5):
    """Glass plate. Low fill alpha so the stage shows through."""
    if pad is None:
        pad = int(28 * height / 176)
    im = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    g = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    gd = ImageDraw.Draw(g)
    box_g = (pad - 6, pad - 6, width - pad + 6, height - pad + 6)
    rad_g = radius if radius is not None else (height - 2 * pad) / 2
    gd.rounded_rectangle(box_g, radius=rad_g + 6, fill=glow)
    im = Image.alpha_composite(im, g.filter(ImageFilter.GaussianBlur(14)))
    inner = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    d = ImageDraw.Draw(inner)
    box = (pad, pad, width - pad, height - pad)
    rad = radius if radius is not None else (height - 2 * pad) / 2
    d.rounded_rectangle(box, radius=rad, fill=fill)
    gloss_im = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    gld = ImageDraw.Draw(gloss_im)
    gh = int((height - 2 * pad) * 0.42)
    gld.rounded_rectangle(
        (pad + 3, pad + 3, width - pad - 3, pad + gh),
        radius=max(rad * 0.9, 8),
        fill=gloss,
    )
    inner = Image.alpha_composite(inner, gloss_im)
    mask = Image.new("L", (width, height), 0)
    ImageDraw.Draw(mask).rounded_rectangle(box, radius=rad, fill=255)
    r, gc, b, a = inner.split()
    inner = Image.merge("RGBA", (r, gc, b, ImageChops.multiply(a, mask)))
    d = ImageDraw.Draw(inner)
    d.rounded_rectangle(box, radius=rad, outline=rim, width=rim_width)
    d.rounded_rectangle(
        (pad + 6, pad + 6, width - pad - 6, height - pad - 6),
        radius=max(rad - 6, 4),
        outline=(255, 255, 255, 40),
        width=2,
    )
    return Image.alpha_composite(im, inner)


def capsule(width, height, rim, glow, gloss=True):
    return glass_panel(
        width,
        height,
        rim,
        glow,
        fill=(8, 14, 24, 110),
        gloss=(255, 255, 255, 22),
    )


def write_tile():
    specs = (
        ("hud_tile_mail.png", (240, 196, 70, 255), (240, 196, 70, 90)),
        ("hud_tile_friends.png", (50, 220, 255, 255), (50, 220, 255, 90)),
        ("hud_tile_settings.png", (186, 92, 255, 255), (186, 92, 255, 90)),
    )
    for name, rim, glow in specs:
        save(
            glass_panel(
                512,
                512,
                rim,
                glow,
                fill=(8, 14, 24, 110),
                gloss=(255, 255, 255, 22),
                rim_width=12,
                pad=36,
                radius=90,
            ),
            name,
        )



def as_nine_patch(src: Image.Image, inset: int) -> Image.Image:
    w, h = src.size
    out = Image.new("RGBA", (w + 2, h + 2), (0, 0, 0, 0))
    out.paste(src, (1, 1))
    d = ImageDraw.Draw(out)
    d.line([(inset, 0), (w + 1 - inset, 0)], fill=(0, 0, 0, 255))
    d.line([(0, inset), (0, h + 1 - inset)], fill=(0, 0, 0, 255))
    d.line([(inset, h + 1), (w + 1 - inset, h + 1)], fill=(0, 0, 0, 255))
    d.line([(w + 1, inset), (w + 1, h + 1 - inset)], fill=(0, 0, 0, 255))
    return out


def coin():
    s = 512
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    im = Image.alpha_composite(im, glow_layer((s, s), (240, 200, 70, 160), 22))
    d = ImageDraw.Draw(im)
    d.ellipse((56, 56, s - 56, s - 56), fill=(168, 112, 22, 255))
    d.ellipse((70, 70, s - 70, s - 70), fill=(228, 178, 48, 255))
    hi = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    hd = ImageDraw.Draw(hi)
    hd.ellipse((88, 72, s - 88, int(s * 0.62)), fill=(255, 236, 150, 170))
    im = Image.alpha_composite(im, hi.filter(ImageFilter.GaussianBlur(10)))
    d = ImageDraw.Draw(im)
    d.ellipse((56, 56, s - 56, s - 56), outline=(90, 55, 8, 255), width=10)
    d.ellipse((86, 86, s - 86, s - 86), outline=(255, 228, 140, 230), width=6)
    d.ellipse((122, 122, s - 122, s - 122), outline=(150, 95, 18, 255), width=8)
    d.ellipse((136, 136, s - 136, s - 136), fill=(236, 188, 58, 255))
    spark = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    sd = ImageDraw.Draw(spark)
    sd.ellipse((120, 100, 210, 175), fill=(255, 255, 240, 150))
    im = Image.alpha_composite(im, spark.filter(ImageFilter.GaussianBlur(8)))
    return im


def gem():
    s = 512
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    im = Image.alpha_composite(im, glow_layer((s, s), (50, 220, 255, 170), 24))
    d = ImageDraw.Draw(im)
    cx, cy = s / 2, s / 2 + 8
    top = (cx, 70)
    left = (80, 230)
    right = (s - 80, 230)
    mid = (cx, 250)
    bot = (cx, s - 70)
    d.polygon([top, right, mid, left], fill=(180, 250, 255, 255))
    d.polygon([left, mid, bot], fill=(18, 110, 180, 255))
    d.polygon([right, mid, bot], fill=(90, 230, 255, 255))
    d.polygon([top, left, mid], fill=(120, 220, 255, 255))
    d.polygon([top, right, mid], fill=(230, 255, 255, 230))
    d.line([top, right, bot, left, top], fill=(230, 255, 255, 255), width=6)
    d.line([left, mid, right], fill=(200, 250, 255, 200), width=4)
    d.line([top, mid, bot], fill=(255, 255, 255, 160), width=3)
    spark = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    sd = ImageDraw.Draw(spark)
    sd.ellipse((170, 110, 230, 165), fill=(255, 255, 255, 180))
    im = Image.alpha_composite(im, spark.filter(ImageFilter.GaussianBlur(6)))
    return im


def gold_metal(mask: Image.Image, light=(255, 244, 196), dark=(168, 108, 22)) -> Image.Image:
    mask = mask.convert("L")
    w, h = mask.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    mp = mask.load()
    px = out.load()
    for y in range(h):
        ty = y / max(h - 1, 1)
        base = mix((*light, 255), (*dark, 255), ty * 0.78)
        for x in range(w):
            a = mp[x, y]
            if not a:
                continue
            hx = 1 - min(math.hypot(x / w - 0.34, y / h - 0.26) * 1.7, 1)
            px[x, y] = (
                min(255, int(base[0] + hx * 42)),
                min(255, int(base[1] + hx * 28)),
                min(255, int(base[2] + hx * 12)),
                a,
            )
    edge = mask.filter(ImageFilter.FIND_EDGES).point(lambda v: min(255, v * 2))
    shade = Image.new("RGBA", (w, h), (70, 42, 6, 0))
    shade.putalpha(edge)
    out = Image.alpha_composite(out, shade)
    spec = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    sd = ImageDraw.Draw(spec)
    sd.ellipse((int(w * 0.28), int(h * 0.18), int(w * 0.48), int(h * 0.34)), fill=(255, 255, 240, 70))
    spec.putalpha(ImageChops.multiply(spec.split()[-1], mask))
    return Image.alpha_composite(out, spec.filter(ImageFilter.GaussianBlur(6)))


def glass_circle() -> Image.Image:
    s = 512
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    im = Image.alpha_composite(im, glow_layer((s, s), (240, 196, 70, 80), 14))
    d = ImageDraw.Draw(im)
    pad = 44
    box = (pad, pad, s - pad, s - pad)
    d.ellipse(box, fill=(6, 10, 20, 245))
    gloss = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    gd = ImageDraw.Draw(gloss)
    gd.ellipse((pad + 22, pad + 14, s - pad - 22, int(s * 0.54)), fill=(255, 255, 255, 34))
    im = Image.alpha_composite(im, gloss.filter(ImageFilter.GaussianBlur(2)))
    inner = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    idr = ImageDraw.Draw(inner)
    idr.ellipse((pad + 28, pad + 28, s - pad - 28, s - pad - 28), fill=(240, 196, 70, 28))
    im = Image.alpha_composite(im, inner.filter(ImageFilter.GaussianBlur(18)))
    d = ImageDraw.Draw(im)
    d.ellipse(box, outline=(90, 58, 10, 255), width=16)
    d.ellipse((pad + 6, pad + 6, s - pad - 6, s - pad - 6), outline=(255, 220, 120, 255), width=7)
    d.ellipse((pad + 16, pad + 16, s - pad - 16, s - pad - 16), outline=(255, 244, 196, 90), width=3)
    return im


def stamp(icon: Image.Image) -> Image.Image:
    return Image.alpha_composite(glass_circle(), icon)


def mail_button() -> Image.Image:
    s = 512
    cx, cy = s / 2, s / 2 + 8
    w, h = 228, 150
    body = Image.new("L", (s, s), 0)
    d = ImageDraw.Draw(body)
    box = (cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2)
    d.rounded_rectangle(box, radius=22, fill=255)
    paper = Image.new("L", (s, s), 0)
    pd = ImageDraw.Draw(paper)
    pd.rounded_rectangle((cx - 86, cy - 40, cx + 86, cy + 44), radius=8, fill=255)
    flap = Image.new("L", (s, s), 0)
    fd = ImageDraw.Draw(flap)
    fd.polygon(
        [
            (cx - w / 2 + 6, cy - h / 2 + 6),
            (cx, cy + 26),
            (cx + w / 2 - 6, cy - h / 2 + 6),
        ],
        fill=255,
    )
    icon = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    icon = Image.alpha_composite(icon, gold_metal(body, dark=(140, 88, 16)))
    sheet = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    sp = sheet.load()
    mp = paper.load()
    for y in range(s):
        for x in range(s):
            if mp[x, y]:
                sp[x, y] = (255, 246, 214, 220)
    icon = Image.alpha_composite(icon, sheet)
    icon = Image.alpha_composite(icon, gold_metal(flap, light=(255, 250, 210), dark=(196, 140, 32)))
    fold = ImageDraw.Draw(icon)
    fold.line(
        [(cx - w / 2 + 10, cy - h / 2 + 10), (cx, cy + 22), (cx + w / 2 - 10, cy - h / 2 + 10)],
        fill=(90, 55, 8, 180),
        width=6,
    )
    return stamp(icon)


def friends_button() -> Image.Image:
    s = 512
    mask = Image.new("L", (s, s), 0)
    d = ImageDraw.Draw(mask)
    cx, cy = s / 2, s / 2
    d.ellipse((cx - 58, cy - 122, cx + 58, cy - 4), fill=255)
    d.rounded_rectangle((cx - 20, cy - 18, cx + 20, cy + 22), radius=10, fill=255)
    d.rounded_rectangle((cx - 124, cy + 10, cx + 124, cy + 188), radius=78, fill=255)
    clip = Image.new("L", (s, s), 0)
    ImageDraw.Draw(clip).ellipse((86, 86, s - 86, s - 86), fill=255)
    mask = ImageChops.multiply(mask, clip)
    return stamp(gold_metal(mask))


def settings_button() -> Image.Image:
    s = 512
    cx, cy = s / 2, s / 2
    teeth = Image.new("L", (s, s), 0)
    td = ImageDraw.Draw(teeth)
    td.rounded_rectangle((cx - 28, 118, cx + 28, s - 118), radius=10, fill=255)
    acc = Image.new("L", (s, s), 0)
    for ang in range(0, 180, 30):
        acc = ImageChops.lighter(acc, teeth.rotate(ang, resample=Image.Resampling.BICUBIC, center=(cx, cy)))
    body = Image.new("L", (s, s), 0)
    bd = ImageDraw.Draw(body)
    bd.ellipse((cx - 108, cy - 108, cx + 108, cy + 108), fill=255)
    hole = Image.new("L", (s, s), 0)
    hd = ImageDraw.Draw(hole)
    hd.ellipse((cx - 44, cy - 44, cx + 44, cy + 44), fill=255)
    ring = Image.new("L", (s, s), 0)
    rd = ImageDraw.Draw(ring)
    rd.ellipse((cx - 58, cy - 58, cx + 58, cy + 58), fill=255)
    mask = ImageChops.subtract(ImageChops.lighter(acc, body), hole)
    mask = ImageChops.lighter(mask, ImageChops.subtract(ring, hole))
    return stamp(gold_metal(mask))


def plus(glow=(240, 196, 70, 150), fill=(196, 140, 28, 255), shine=(255, 230, 130, 90), outline=(255, 236, 160, 255), mark=(40, 24, 4, 255)):
    s = 256
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    im = Image.alpha_composite(im, glow_layer((s, s), glow, 16, shape="pill"))
    d = ImageDraw.Draw(im)
    pad = 28
    d.rounded_rectangle((pad, pad, s - pad, s - pad), radius=36, fill=fill)
    gold = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    gd = ImageDraw.Draw(gold)
    gd.rounded_rectangle((pad, pad, s - pad, int(s * 0.58)), radius=32, fill=shine)
    im = Image.alpha_composite(im, gold)
    d = ImageDraw.Draw(im)
    d.rounded_rectangle((pad, pad, s - pad, s - pad), radius=36, outline=outline, width=5)
    cx, cy = s / 2, s / 2
    bar = 22
    arm = 58
    d.rounded_rectangle((cx - bar / 2, cy - arm, cx + bar / 2, cy + arm), radius=8, fill=mark)
    d.rounded_rectangle((cx - arm, cy - bar / 2, cx + arm, cy + bar / 2), radius=8, fill=mark)
    return im


def save(im: Image.Image, name: str):
    path = OUT / name
    im.save(path, "PNG")
    print("wrote", path, im.size)


def write_buttons():
    OUT.mkdir(parents=True, exist_ok=True)
    save(mail_button(), "hud_mail.png")
    save(friends_button(), "hud_friends.png")
    save(settings_button(), "hud_settings.png")


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    save(capsule(720, 176, (240, 196, 70, 255), (240, 196, 70, 90)), "hud_chip_gold.png")
    save(capsule(720, 176, (50, 220, 255, 255), (50, 220, 255, 90)), "hud_chip_cyan.png")
    for stale in ("hud_chip_gold.9.png", "hud_chip_cyan.9.png"):
        p = OUT / stale
        if p.exists():
            p.unlink()
    save(coin(), "hud_coin.png")
    save(gem(), "hud_gem.png")
    save(plus(), "hud_plus.png")
    save(
        plus(
            glow=(50, 220, 255, 150),
            fill=(18, 150, 190, 255),
            shine=(160, 245, 255, 90),
            outline=(180, 250, 255, 255),
            mark=(4, 32, 48, 255),
        ),
        "hud_plus_cyan.png",
    )
    write_tile()
    write_buttons()


if __name__ == "__main__":
    import sys
    if "--buttons" in sys.argv:
        write_buttons()
    else:
        main()
