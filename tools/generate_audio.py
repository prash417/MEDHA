import math
import os
import struct
import wave
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "app" / "src" / "main" / "res" / "raw"
FONT = ROOT / "app" / "src" / "main" / "res" / "font"
RAW.mkdir(parents=True, exist_ok=True)
FONT.mkdir(parents=True, exist_ok=True)

SR = 22050


def clamp(v):
    return max(-1.0, min(1.0, v))


def write_wav(name, samples, sample_rate=SR):
    path = RAW / f"{name}.wav"
    with wave.open(str(path), "w") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(sample_rate)
        frames = b"".join(struct.pack("<h", int(clamp(s) * 32767)) for s in samples)
        w.writeframes(frames)
    print("wrote", path.name, f"{len(samples)/sample_rate:.2f}s")


def env(i, n, a=0.01, r=0.12):
    t = i / n
    attack = min(1.0, t / a) if a > 0 else 1.0
    release = min(1.0, (1.0 - t) / r) if r > 0 else 1.0
    return attack * release


def tone(freq, dur, amp=0.28, a=0.01, r=0.18, sr=SR):
    n = int(sr * dur)
    out = []
    for i in range(n):
        t = i / sr
        s = math.sin(2 * math.pi * freq * t)
        s += 0.18 * math.sin(2 * math.pi * freq * 2 * t)
        out.append(s * amp * env(i, n, a, r))
    return out


def noise(dur, amp=0.08, sr=SR):
    n = int(sr * dur)
    x = 0.0
    out = []
    for i in range(n):
        x = (x * 0.96) + ((hash((i, 17)) % 1000) / 500.0 - 1.0) * 0.25
        out.append(x * amp * env(i, n, 0.002, 0.3))
    return out


def mix(*tracks):
    n = max(len(t) for t in tracks)
    out = [0.0] * n
    for t in tracks:
        for i, v in enumerate(t):
            out[i] += v
    peak = max(0.001, max(abs(v) for v in out))
    if peak > 0.95:
        out = [v * 0.95 / peak for v in out]
    return out


def pad(samples, dur, sr=SR):
    n = int(sr * dur)
    if len(samples) >= n:
        return samples[:n]
    return samples + [0.0] * (n - len(samples))


def silence(dur, sr=SR):
    return [0.0] * int(sr * dur)


# Distinctive MEDHA palette: C3 drone, E4/G4/B4 intellect tones, copper fifths.
SA, RE, GA, PA, DHA, NI, SA2 = 130.81, 146.83, 164.81, 196.00, 220.00, 246.94, 261.63


def sfx_launch():
    return mix(
        tone(SA, 1.4, 0.22, 0.08, 0.5),
        tone(PA, 1.2, 0.16, 0.2, 0.45),
        pad(silence(0.45) + tone(SA2, 0.7, 0.18, 0.01, 0.4), 1.4),
    )


def sfx_tap():
    return mix(tone(NI * 2, 0.07, 0.18, 0.002, 0.4), tone(SA2 * 2, 0.05, 0.1, 0.001, 0.5))


def sfx_select():
    return mix(tone(GA * 2, 0.16, 0.2, 0.005, 0.4), tone(PA * 2, 0.22, 0.14, 0.02, 0.45))


def sfx_question():
    return mix(
        tone(PA, 0.55, 0.16, 0.02, 0.5),
        pad(silence(0.08) + tone(SA2, 0.4, 0.14, 0.01, 0.5), 0.55),
    )


def sfx_suspense():
    n = int(SR * 1.35)
    out = []
    for i in range(n):
        t = i / SR
        pulse = 0.5 + 0.5 * math.sin(2 * math.pi * 2.4 * t)
        s = math.sin(2 * math.pi * 55 * t) * 0.22 * pulse
        s += math.sin(2 * math.pi * 82.5 * t) * 0.1 * pulse
        out.append(s * env(i, n, 0.05, 0.2))
    return out


def sfx_heartbeat():
    def thump(f, d, amp):
        n = int(SR * d)
        o = []
        for i in range(n):
            t = i / SR
            o.append(math.sin(2 * math.pi * f * t) * amp * math.exp(-t * 18))
        return o

    return mix(thump(48, 0.22, 0.55), pad(silence(0.16) + thump(42, 0.28, 0.42), 0.5))


def sfx_lifeline():
    return mix(
        tone(PA * 2, 0.22, 0.16, 0.005, 0.4),
        pad(silence(0.08) + tone(DHA * 2, 0.22, 0.14, 0.005, 0.4), 0.4),
        pad(silence(0.16) + tone(SA2 * 2, 0.28, 0.18, 0.005, 0.45), 0.5),
    )


def sfx_correct():
    return mix(
        tone(SA2, 0.22, 0.18, 0.005, 0.35),
        pad(silence(0.08) + tone(GA * 2, 0.22, 0.16, 0.005, 0.35), 0.5),
        pad(silence(0.16) + tone(PA * 2, 0.28, 0.16, 0.005, 0.4), 0.6),
        pad(silence(0.26) + tone(SA2 * 2, 0.45, 0.2, 0.005, 0.45), 0.8),
    )


def sfx_wrong():
    return mix(
        tone(196, 0.28, 0.2, 0.005, 0.4),
        pad(silence(0.12) + tone(155, 0.35, 0.18, 0.01, 0.45), 0.55),
        pad(silence(0.24) + tone(110, 0.55, 0.22, 0.02, 0.5), 0.85),
    )


def sfx_prize():
    return mix(
        tone(PA * 2, 0.18, 0.14, 0.002, 0.4),
        pad(silence(0.06) + tone(NI * 2, 0.18, 0.14, 0.002, 0.4), 0.3),
        pad(silence(0.12) + tone(SA2 * 2, 0.28, 0.18, 0.002, 0.4), 0.45),
    )


def sfx_progress():
    return mix(tone(GA * 2, 0.12, 0.14, 0.002, 0.4), pad(silence(0.07) + tone(PA * 2, 0.18, 0.16, 0.002, 0.4), 0.28))


def sfx_victory():
    notes = [SA2, GA * 2, PA * 2, NI * 2, SA2 * 2]
    parts = []
    t = 0.0
    for n in notes:
        parts.append(pad(silence(t) + tone(n, 0.32, 0.16, 0.01, 0.4), 2.0))
        t += 0.14
    parts.append(pad(silence(0.7) + tone(SA2 * 2, 0.9, 0.18, 0.02, 0.5), 2.0))
    return mix(*parts)


def sfx_gameover():
    return mix(
        tone(PA, 0.5, 0.16, 0.04, 0.45),
        pad(silence(0.2) + tone(GA, 0.55, 0.14, 0.04, 0.5), 0.9),
        pad(silence(0.4) + tone(SA, 0.9, 0.18, 0.08, 0.55), 1.4),
    )


def sfx_timer():
    return tone(SA2 * 2, 0.08, 0.12, 0.001, 0.5)


def loop_pad(dur, freqs, amp=0.08):
    n = int(SR * dur)
    out = []
    for i in range(n):
        t = i / SR
        s = 0.0
        for j, f in enumerate(freqs):
            s += math.sin(2 * math.pi * f * t + j) * (amp / len(freqs))
            s += 0.15 * math.sin(2 * math.pi * f * 0.5 * t)
        lfo = 0.75 + 0.25 * math.sin(2 * math.pi * 0.07 * t)
        fade = min(1.0, t / 0.4, (dur - t) / 0.4)
        out.append(s * lfo * fade)
    return out


def main():
    write_wav("sfx_launch", sfx_launch())
    write_wav("sfx_tap", sfx_tap())
    write_wav("sfx_select", sfx_select())
    write_wav("sfx_question", sfx_question())
    write_wav("sfx_suspense", sfx_suspense())
    write_wav("sfx_heartbeat", sfx_heartbeat())
    write_wav("sfx_lifeline", sfx_lifeline())
    write_wav("sfx_correct", sfx_correct())
    write_wav("sfx_wrong", sfx_wrong())
    write_wav("sfx_prize", sfx_prize())
    write_wav("sfx_progress", sfx_progress())
    write_wav("sfx_victory", sfx_victory())
    write_wav("sfx_gameover", sfx_gameover())
    write_wav("sfx_timer", sfx_timer())
    write_wav("music_lobby", loop_pad(8.0, [SA, PA, SA2], 0.09))
    write_wav("music_gameplay", loop_pad(8.0, [SA, GA, PA], 0.07))
    write_wav("music_highstakes", loop_pad(8.0, [98.0, 147.0, 196.0], 0.1))
    write_wav("music_victory", loop_pad(6.0, [SA2, PA * 2, SA2 * 2], 0.08))
    write_wav("music_gameover", loop_pad(6.0, [110.0, 146.8], 0.07))

    fonts = {
        "cormorant_garamond.ttf": "https://github.com/google/fonts/raw/main/ofl/cormorantgaramond/CormorantGaramond%5Bwght%5D.ttf",
        "outfit.ttf": "https://github.com/google/fonts/raw/main/ofl/outfit/Outfit%5Bwght%5D.ttf",
    }
    for name, url in fonts.items():
        dest = FONT / name
        if dest.exists() and dest.stat().st_size > 1000:
            print("font exists", name)
            continue
        try:
            urllib.request.urlretrieve(url, dest)
            print("downloaded", name, dest.stat().st_size)
        except Exception as e:
            print("font download failed", name, e)


if __name__ == "__main__":
    main()
