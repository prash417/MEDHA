"""
Builds MEDHA's 3D coin and trophy as .glb files (glTF 2.0, PBR gold), using only numpy.

    python tools/models/make_models.py

Writes app/src/main/assets/models/coin.glb and trophy.glb. Tweak the profiles below
to change the shapes; every part is a "lathe" (a 2D outline spun round the Y axis)
except the star and the trophy handles.
"""
import json, math, os, struct, sys
import numpy as np

OUT = os.path.join(os.path.dirname(__file__), "..", "..", "app", "src", "main", "assets", "models")

GOLD = {"name": "gold", "pbrMetallicRoughness": {"baseColorFactor": [1.0, 0.76, 0.33, 1.0], "metallicFactor": 1.0, "roughnessFactor": 0.28}}
GOLD_BRIGHT = {"name": "gold_bright", "pbrMetallicRoughness": {"baseColorFactor": [1.0, 0.84, 0.45, 1.0], "metallicFactor": 1.0, "roughnessFactor": 0.18}}
LACQUER = {"name": "lacquer", "pbrMetallicRoughness": {"baseColorFactor": [0.03, 0.09, 0.26, 1.0], "metallicFactor": 0.3, "roughnessFactor": 0.32}}


def lathe(profile, segments=72, hard=()):
    """profile: list of (r, y) from top-centre outward and round. hard: indexes with sharp edges."""
    pts = [np.array(p, dtype=np.float64) for p in profile]
    n = len(pts)
    seg_n = []
    for i in range(n - 1):
        d = pts[i + 1] - pts[i]
        nn = np.array([-d[1], d[0]])
        ln = np.linalg.norm(nn)
        seg_n.append(nn / ln if ln > 0 else np.array([0.0, 1.0]))
    # each profile point emits one or two (point, normal) pairs
    rows = []  # list of (point, normal, belongs_to_segment_start?)
    ring_for_seg = []  # (ring index at segment start, ring index at segment end)
    last_end = None
    for i in range(n - 1):
        a, b = pts[i], pts[i + 1]
        na = seg_n[i]
        nb = seg_n[i]
        if i > 0 and i not in hard:
            s = seg_n[i - 1] + seg_n[i]
            na = s / (np.linalg.norm(s) or 1)
        if i + 1 < n - 1 and (i + 1) not in hard:
            s = seg_n[i] + seg_n[i + 1]
            nb = s / (np.linalg.norm(s) or 1)
        if last_end is not None and i not in hard:
            ia = last_end
        else:
            rows.append((a, na)); ia = len(rows) - 1
        rows.append((b, nb)); ib = len(rows) - 1
        last_end = ib
        ring_for_seg.append((ia, ib))
    verts, norms = [], []
    for (p, nr) in rows:
        for k in range(segments + 1):
            t = 2 * math.pi * k / segments
            c, s_ = math.cos(t), math.sin(t)
            verts.append((p[0] * c, p[1], p[0] * s_))
            norms.append((nr[0] * c, nr[1], nr[0] * s_))
    idx = []
    W = segments + 1
    for (ia, ib) in ring_for_seg:
        for k in range(segments):
            a0 = ia * W + k; a1 = a0 + 1
            b0 = ib * W + k; b1 = b0 + 1
            idx += [a0, b0, a1, a1, b0, b1]
    return np.array(verts, np.float32), np.array(norms, np.float32), np.array(idx, np.uint32)


def fix_winding(v, nrm, idx):
    """Make every triangle face the way its vertex normals point."""
    tri = idx.reshape(-1, 3).copy()
    p0, p1, p2 = v[tri[:, 0]], v[tri[:, 1]], v[tri[:, 2]]
    fn = np.cross(p1 - p0, p2 - p0)
    avg = nrm[tri[:, 0]] + nrm[tri[:, 1]] + nrm[tri[:, 2]]
    flip = (fn * avg).sum(1) < 0
    tri[flip] = tri[flip][:, [0, 2, 1]]
    return tri.reshape(-1).astype(np.uint32)


def star_prism(r_out, r_in, y0, y1, points=5):
    """Raised star on a coin face, from height y0 to y1 (y1 is the top)."""
    ring = []
    for k in range(points * 2):
        ang = math.pi / 2 + k * math.pi / points
        r = r_out if k % 2 == 0 else r_in
        ring.append((r * math.cos(ang), r * math.sin(ang)))
    v, nrm, idx = [], [], []
    up = 1.0 if y1 > y0 else -1.0
    # top cap: fan from the centre
    c = len(v); v.append((0, y1, 0)); nrm.append((0, up, 0))
    for (x, z) in ring:
        v.append((x, y1, z)); nrm.append((0, up, 0))
    m = len(ring)
    for k in range(m):
        idx += [c, c + 1 + k, c + 1 + (k + 1) % m]
    # side walls with flat normals
    for k in range(m):
        (xa, za), (xb, zb) = ring[k], ring[(k + 1) % m]
        ex, ez = xb - xa, zb - za
        nx, nz = ez, -ex
        ln = math.hypot(nx, nz) or 1
        nx, nz = nx / ln, nz / ln
        if nx * (xa + xb) + nz * (za + zb) < 0:
            nx, nz = -nx, -nz
        b = len(v)
        for (x, z, y) in ((xa, za, y0), (xb, zb, y0), (xa, za, y1), (xb, zb, y1)):
            v.append((x, y, z)); nrm.append((nx, 0, nz))
        idx += [b, b + 2, b + 1, b + 1, b + 2, b + 3]
    return np.array(v, np.float32), np.array(nrm, np.float32), np.array(idx, np.uint32)


def handle(cx, cy, radius, tube, a0, a1, steps=40, sides=20, mirror=False):
    """Curved tube along an arc in the XY plane: a trophy handle."""
    v, nrm, idx = [], [], []
    for i in range(steps + 1):
        t = a0 + (a1 - a0) * i / steps
        px, py = cx + radius * math.cos(t), cy + radius * math.sin(t)
        nx, ny = math.cos(t), math.sin(t)  # points away from arc centre
        for j in range(sides + 1):
            f = 2 * math.pi * j / sides
            ox, oy, oz = nx * math.cos(f), ny * math.cos(f), math.sin(f)
            x = px + tube * ox
            if mirror:
                v.append((-x, py + tube * oy, tube * oz)); nrm.append((-ox, oy, oz))
            else:
                v.append((x, py + tube * oy, tube * oz)); nrm.append((ox, oy, oz))
    W = sides + 1
    for i in range(steps):
        for j in range(sides):
            a = i * W + j; b = a + W
            idx += [a, b, a + 1, a + 1, b, b + 1]
    return np.array(v, np.float32), np.array(nrm, np.float32), np.array(idx, np.uint32)


def merge(parts):
    vs, ns, ids, off = [], [], [], 0
    for v, n, i in parts:
        vs.append(v); ns.append(n); ids.append(i + off); off += len(v)
    return np.concatenate(vs), np.concatenate(ns), np.concatenate(ids).astype(np.uint32)


def write_glb(path, meshes, rotation=None):
    """meshes: list of (verts, normals, indices, material dict)."""
    materials, mat_index = [], {}
    bin_chunks, buffer_views, accessors, primitives = [], [], [], []
    offset = 0

    def add_view(data, target):
        nonlocal offset
        pad = (-len(data)) % 4
        bin_chunks.append(data + b"\0" * pad)
        buffer_views.append({"buffer": 0, "byteOffset": offset, "byteLength": len(data), "target": target})
        offset += len(data) + pad
        return len(buffer_views) - 1

    for v, n, i, mat in meshes:
        i = fix_winding(v, n, i)
        n = n / np.maximum(np.linalg.norm(n, axis=1, keepdims=True), 1e-8)
        if mat["name"] not in mat_index:
            mat_index[mat["name"]] = len(materials); materials.append(mat)
        pv = add_view(v.astype(np.float32).tobytes(), 34962)
        nv = add_view(n.astype(np.float32).tobytes(), 34962)
        iv = add_view(i.astype(np.uint32).tobytes(), 34963)
        accessors.append({"bufferView": pv, "componentType": 5126, "count": len(v), "type": "VEC3",
                          "min": v.min(0).tolist(), "max": v.max(0).tolist()})
        pa = len(accessors) - 1
        accessors.append({"bufferView": nv, "componentType": 5126, "count": len(n), "type": "VEC3"})
        na = len(accessors) - 1
        accessors.append({"bufferView": iv, "componentType": 5125, "count": len(i), "type": "SCALAR"})
        ia = len(accessors) - 1
        primitives.append({"attributes": {"POSITION": pa, "NORMAL": na}, "indices": ia, "material": mat_index[mat["name"]]})

    node = {"mesh": 0, "name": os.path.splitext(os.path.basename(path))[0]}
    if rotation:
        node["rotation"] = rotation
    gltf = {
        "asset": {"version": "2.0", "generator": "MEDHA make_models.py"},
        "scene": 0, "scenes": [{"nodes": [0]}], "nodes": [node],
        "meshes": [{"primitives": primitives}],
        "materials": materials,
        "buffers": [{"byteLength": offset}],
        "bufferViews": buffer_views, "accessors": accessors,
    }
    js = json.dumps(gltf, separators=(",", ":")).encode()
    js += b" " * ((-len(js)) % 4)
    binary = b"".join(bin_chunks)
    total = 12 + 8 + len(js) + 8 + len(binary)
    with open(path, "wb") as f:
        f.write(struct.pack("<III", 0x46546C67, 2, total))
        f.write(struct.pack("<II", len(js), 0x4E4F534A)); f.write(js)
        f.write(struct.pack("<II", len(binary), 0x004E4942)); f.write(binary)
    print(f"wrote {path}  ({total/1024:.0f} KB)")


def coin():
    t = 0.07  # half thickness of the field
    profile = [(0.0, t), (0.80, t), (0.83, t + 0.045), (0.95, t + 0.045), (1.0, t + 0.01),
               (1.0, -t - 0.01), (0.95, -t - 0.045), (0.83, -t - 0.045), (0.80, -t), (0.0, -t)]
    body = lathe(profile, segments=96, hard=(1, 2, 3, 4, 5, 6, 7, 8))
    star_top = star_prism(0.52, 0.22, t - 0.001, t + 0.05)
    star_bot = star_prism(0.52, 0.22, -t + 0.001, -t - 0.05)
    # stand the coin up so its faces look along Z (towards the camera)
    rot = [math.sin(math.pi / 4), 0, 0, math.cos(math.pi / 4)]
    write_glb(os.path.join(OUT, "coin.glb"), [
        (*body, GOLD),
        (*merge([star_top, star_bot]), GOLD_BRIGHT),
    ], rotation=rot)


def trophy():
    base = [(0.0, 0.34), (0.36, 0.34), (0.42, 0.30), (0.42, 0.20), (0.52, 0.18), (0.60, 0.16),
            (0.60, 0.0), (0.0, 0.0)]
    # outline from the stem up the outside of the cup, over the lip and down inside
    cup = [(0.0, 0.34), (0.17, 0.34), (0.11, 0.42), (0.09, 0.60), (0.17, 0.65), (0.17, 0.70),
           (0.10, 0.75), (0.13, 0.81), (0.30, 0.87), (0.43, 0.98), (0.51, 1.12), (0.55, 1.28),
           (0.58, 1.38), (0.54, 1.40), (0.51, 1.34), (0.47, 1.18), (0.39, 1.04), (0.20, 0.97), (0.0, 0.95)]
    b = lathe(base, segments=72, hard=(1, 2, 3, 4, 5, 6))
    c = lathe(cup, segments=72, hard=(1, 4, 5, 12, 13))
    h1 = handle(0.50, 1.10, 0.20, 0.045, math.radians(100), math.radians(-85))
    h2 = handle(0.50, 1.10, 0.20, 0.045, math.radians(100), math.radians(-85), mirror=True)
    star = star_prism(0.20, 0.085, 0.0, 0.03)
    # put the star on the front of the cup, facing +Z
    sv, sn, si = star
    sv = sv[:, [0, 2, 1]].copy(); sn = sn[:, [0, 2, 1]].copy()
    sv[:, 1] += 1.12; sv[:, 2] += 0.50
    write_glb(os.path.join(OUT, "trophy.glb"), [
        (*b, LACQUER),
        (*merge([c, h1, h2]), GOLD),
        (sv, sn, si, GOLD_BRIGHT),
    ])


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    coin()
    trophy()
