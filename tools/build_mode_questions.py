"""Build 500 original questions each for classic, quick, and practice."""
from __future__ import annotations

import json
import random
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "mode_questions.json"
EXISTING = ROOT / "questions.json"

ELEMENTS = """
1|H|Hydrogen
2|He|Helium
3|Li|Lithium
4|Be|Beryllium
5|B|Boron
6|C|Carbon
7|N|Nitrogen
8|O|Oxygen
9|F|Fluorine
10|Ne|Neon
11|Na|Sodium
12|Mg|Magnesium
13|Al|Aluminium
14|Si|Silicon
15|P|Phosphorus
16|S|Sulfur
17|Cl|Chlorine
18|Ar|Argon
19|K|Potassium
20|Ca|Calcium
21|Sc|Scandium
22|Ti|Titanium
23|V|Vanadium
24|Cr|Chromium
25|Mn|Manganese
26|Fe|Iron
27|Co|Cobalt
28|Ni|Nickel
29|Cu|Copper
30|Zn|Zinc
31|Ga|Gallium
32|Ge|Germanium
33|As|Arsenic
34|Se|Selenium
35|Br|Bromine
36|Kr|Krypton
37|Rb|Rubidium
38|Sr|Strontium
39|Y|Yttrium
40|Zr|Zirconium
41|Nb|Niobium
42|Mo|Molybdenum
43|Tc|Technetium
44|Ru|Ruthenium
45|Rh|Rhodium
46|Pd|Palladium
47|Ag|Silver
48|Cd|Cadmium
49|In|Indium
50|Sn|Tin
51|Sb|Antimony
52|Te|Tellurium
53|I|Iodine
54|Xe|Xenon
55|Cs|Caesium
56|Ba|Barium
57|La|Lanthanum
58|Ce|Cerium
59|Pr|Praseodymium
60|Nd|Neodymium
61|Pm|Promethium
62|Sm|Samarium
63|Eu|Europium
64|Gd|Gadolinium
65|Tb|Terbium
66|Dy|Dysprosium
67|Ho|Holmium
68|Er|Erbium
69|Tm|Thulium
70|Yb|Ytterbium
71|Lu|Lutetium
72|Hf|Hafnium
73|Ta|Tantalum
74|W|Tungsten
75|Re|Rhenium
76|Os|Osmium
77|Ir|Iridium
78|Pt|Platinum
79|Au|Gold
80|Hg|Mercury
81|Tl|Thallium
82|Pb|Lead
83|Bi|Bismuth
84|Po|Polonium
85|At|Astatine
86|Rn|Radon
87|Fr|Francium
88|Ra|Radium
89|Ac|Actinium
90|Th|Thorium
91|Pa|Protactinium
92|U|Uranium
93|Np|Neptunium
94|Pu|Plutonium
95|Am|Americium
96|Cm|Curium
97|Bk|Berkelium
98|Cf|Californium
99|Es|Einsteinium
100|Fm|Fermium
101|Md|Mendelevium
102|No|Nobelium
103|Lr|Lawrencium
104|Rf|Rutherfordium
105|Db|Dubnium
106|Sg|Seaborgium
107|Bh|Bohrium
108|Hs|Hassium
109|Mt|Meitnerium
110|Ds|Darmstadtium
111|Rg|Roentgenium
112|Cn|Copernicium
113|Nh|Nihonium
114|Fl|Flerovium
115|Mc|Moscovium
116|Lv|Livermorium
117|Ts|Tennessine
118|Og|Oganesson
""".strip().splitlines()

# country|currency|iso2|calling
PLACES = """
India|Indian rupee|IN|+91
Japan|Japanese yen|JP|+81
United States|US dollar|US|+1
United Kingdom|Pound sterling|GB|+44
China|Chinese yuan|CN|+86
France|Euro|FR|+33
Germany|Euro|DE|+49
Italy|Euro|IT|+39
Spain|Euro|ES|+34
Brazil|Brazilian real|BR|+55
Canada|Canadian dollar|CA|+1
Australia|Australian dollar|AU|+61
Russia|Russian ruble|RU|+7
Mexico|Mexican peso|MX|+52
South Korea|South Korean won|KR|+82
Indonesia|Indonesian rupiah|ID|+62
Turkey|Turkish lira|TR|+90
Saudi Arabia|Saudi riyal|SA|+966
South Africa|South African rand|ZA|+27
Nigeria|Nigerian naira|NG|+234
Egypt|Egyptian pound|EG|+20
Argentina|Argentine peso|AR|+54
Sweden|Swedish krona|SE|+46
Norway|Norwegian krone|NO|+47
Switzerland|Swiss franc|CH|+41
Netherlands|Euro|NL|+31
Belgium|Euro|BE|+32
Portugal|Euro|PT|+351
Poland|Polish zloty|PL|+48
Greece|Euro|GR|+30
Thailand|Thai baht|TH|+66
Vietnam|Vietnamese dong|VN|+84
Malaysia|Malaysian ringgit|MY|+60
Singapore|Singapore dollar|SG|+65
Philippines|Philippine peso|PH|+63
Pakistan|Pakistani rupee|PK|+92
Bangladesh|Bangladeshi taka|BD|+880
Sri Lanka|Sri Lankan rupee|LK|+94
Nepal|Nepalese rupee|NP|+977
New Zealand|New Zealand dollar|NZ|+64
Ireland|Euro|IE|+353
Denmark|Danish krone|DK|+45
Finland|Euro|FI|+358
Austria|Euro|AT|+43
Czech Republic|Czech koruna|CZ|+420
Hungary|Hungarian forint|HU|+36
Romania|Romanian leu|RO|+40
Ukraine|Ukrainian hryvnia|UA|+380
Israel|Israeli shekel|IL|+972
United Arab Emirates|UAE dirham|AE|+971
Qatar|Qatari riyal|QA|+974
Kuwait|Kuwaiti dinar|KW|+965
Iran|Iranian rial|IR|+98
Iraq|Iraqi dinar|IQ|+964
Kenya|Kenyan shilling|KE|+254
Ethiopia|Ethiopian birr|ET|+251
Morocco|Moroccan dirham|MA|+212
Chile|Chilean peso|CL|+56
Colombia|Colombian peso|CO|+57
Peru|Peruvian sol|PE|+51
Venezuela|Venezuelan bolivar|VE|+58
Cuba|Cuban peso|CU|+53
Jamaica|Jamaican dollar|JM|+1
Iceland|Icelandic krona|IS|+354
Croatia|Euro|HR|+385
Serbia|Serbian dinar|RS|+381
Bulgaria|Bulgarian lev|BG|+359
Slovakia|Euro|SK|+421
Slovenia|Euro|SI|+386
Estonia|Euro|EE|+372
Latvia|Euro|LV|+371
Lithuania|Euro|LT|+370
Luxembourg|Euro|LU|+352
Malta|Euro|MT|+356
Cyprus|Euro|CY|+357
""".strip().splitlines()


def lines(block: str) -> list[str]:
    return [ln.strip() for ln in block.strip().splitlines() if ln.strip()]


def diff_for(n: int) -> str:
    if n <= 170:
        return "EASY"
    if n <= 340:
        return "MEDIUM"
    return "HARD"


def pack(prompt: str, correct: str, wrongs: list[str]) -> tuple[list[str], str]:
    opts = [correct] + wrongs[:3]
    rng = random.Random(prompt)
    rng.shuffle(opts)
    letter = "ABCD"[opts.index(correct)]
    return opts, letter


def others(values: list[str], keep: str) -> list[str]:
    def kind(v: str) -> str:
        t = v.strip()
        if t[:1] in "+-" or t.replace(" ", "").replace("or", "").isdigit():
            return "num"
        if len(t) <= 3 and t.replace(" ", "").isalpha():
            return "short"
        return "word"

    want = kind(keep)
    pool = []
    seen = {keep}
    for v in values:
        if v in seen or kind(v) != want:
            continue
        seen.add(v)
        pool.append(v)
    rng = random.Random(keep)
    rng.shuffle(pool)
    if len(pool) < 3:
        for v in values:
            if v in seen:
                continue
            seen.add(v)
            pool.append(v)
            if len(pool) >= 3:
                break
    if len(pool) < 3:
        raise SystemExit(f"not enough distractors for {keep}")
    return pool[:3]


def item(cat: str, n: int, prompt: str, correct: str, wrongs: list[str], explanation: str) -> dict:
    opts, letter = pack(prompt, correct, wrongs)
    return {
        "id": f"{cat}_{n:03d}",
        "question": prompt,
        "prompt": prompt,
        "optionA": opts[0],
        "optionB": opts[1],
        "optionC": opts[2],
        "optionD": opts[3],
        "correctAnswer": letter,
        "correct": letter,
        "category": cat,
        "difficulty": diff_for(n),
        "explanation": explanation,
        "prizeLevel": (n - 1) % 15,
        "language": "en",
        "active": True,
        "tags": [cat, "mode"],
        "source": "medha-mode",
    }


def take(cat: str, rows: list[tuple[str, str, str]], seen: set[str], need: int) -> list[dict]:
    out = []
    answers = [a for _, a, _ in rows]
    for prompt, answer, expl in rows:
        if prompt in seen:
            continue
        seen.add(prompt)
        out.append(item(cat, len(out) + 1, prompt, answer, others(answers, answer), expl))
        if len(out) == need:
            break
    if len(out) < need:
        raise SystemExit(f"{cat} only built {len(out)} of {need}")
    for i, q in enumerate(out, start=1):
        q["id"] = f"{cat}_{i:03d}"
        q["difficulty"] = diff_for(i)
        q["prizeLevel"] = (i - 1) % 15
    return out


def classic_rows() -> list[tuple[str, str, str]]:
    rows = []
    elements = []
    for ln in ELEMENTS:
        num, sym, name = ln.split("|")
        elements.append((int(num), sym, name))
    symbols = [s for _, s, _ in elements]
    names = [n for _, _, n in elements]
    for num, sym, name in elements:
        rows.append((
            f"What is the chemical symbol for {name}?",
            sym,
            f"This is the standard one- or two-letter symbol on the periodic table for {name}.",
        ))
    for num, sym, name in elements:
        rows.append((
            f"What is the atomic number of {name}?",
            str(num),
            f"Atomic number counts the protons in one atom of {name}.",
        ))
    places = [ln.split("|") for ln in PLACES]
    currencies = [p[1] for p in places]
    for country, currency, iso, calling in places:
        rows.append((
            f"Which currency is used in {country}?",
            currency,
            f"This is the money in everyday use in {country}.",
        ))
        rows.append((
            f"What is the two-letter country code for {country}?",
            iso,
            f"This is the ISO alpha-2 code used for {country} on the web and in passports.",
        ))
        rows.append((
            f"What is the international calling code for {country}?",
            calling,
            f"Dial this country code before a phone number in {country}.",
        ))
    planets = [
        ("Mercury", "first"),
        ("Venus", "second"),
        ("Earth", "third"),
        ("Mars", "fourth"),
        ("Jupiter", "fifth"),
        ("Saturn", "sixth"),
        ("Uranus", "seventh"),
        ("Neptune", "eighth"),
    ]
    for name, place in planets:
        rows.append((
            f"Which planet is {place} from the Sun?",
            name,
            f"Counting outward from the Sun, this is the {place} planet.",
        ))
    for num, sym, name in elements:
        rows.append((
            f"Which element has atomic number {num}?",
            name,
            "Atomic number is the number of protons in one atom of that element.",
        ))
        rows.append((
            f"Which element has the chemical symbol {sym}?",
            name,
            "The chemical symbol is the short mark used for that element on the periodic table.",
        ))
    for n in range(2, 31):
        rows.append((
            f"In the classic bank, what is {n} squared?",
            str(n * n),
            f"Squaring means multiplying {n} by itself.",
        ))
    return rows


def roman(n: int) -> str:
    parts = [
        (1000, "M"), (900, "CM"), (500, "D"), (400, "CD"),
        (100, "C"), (90, "XC"), (50, "L"), (40, "XL"),
        (10, "X"), (9, "IX"), (5, "V"), (4, "IV"), (1, "I"),
    ]
    out = []
    for value, glyph in parts:
        while n >= value:
            out.append(glyph)
            n -= value
    return "".join(out)


def quick_rows() -> list[tuple[str, str, str]]:
    rows = []
    sports = [
        ("Cricket", "11"),
        ("Football (soccer)", "11"),
        ("Basketball", "5"),
        ("Volleyball", "6"),
        ("Field hockey", "11"),
        ("Ice hockey", "6"),
        ("Baseball", "9"),
        ("Rugby union", "15"),
        ("Rugby league", "13"),
        ("Water polo", "7"),
        ("Kabaddi", "7"),
        ("Polo", "4"),
        ("Handball", "7"),
        ("Netball", "7"),
        ("American football", "11"),
        ("Lacrosse", "10"),
        ("Beach volleyball", "2"),
        ("Table tennis doubles", "2"),
        ("Tennis doubles", "2"),
        ("Badminton doubles", "2"),
    ]
    for game, count in sports:
        rows.append((
            f"How many players are on the field for one team in {game}?",
            count,
            f"This is the standard on-field team size in {game}.",
        ))
    young = [
        ("cat", "kitten"), ("dog", "puppy"), ("horse", "foal"), ("cow", "calf"),
        ("sheep", "lamb"), ("goat", "kid"), ("duck", "duckling"), ("goose", "gosling"),
        ("swan", "cygnet"), ("eagle", "eaglet"), ("owl", "owlet"), ("lion", "cub"),
        ("tiger", "cub"), ("bear", "cub"), ("deer", "fawn"), ("kangaroo", "joey"),
        ("frog", "tadpole"), ("butterfly", "caterpillar"), ("hen", "chick"),
        ("pig", "piglet"), ("seal", "pup"), ("whale", "calf"), ("elephant", "calf"),
        ("rabbit", "kit"), ("fox", "kit"), ("beaver", "kit"), ("hawk", "eyas"),
        ("pigeon", "squab"), ("salmon", "alevin"), ("eel", "elver"),
    ]
    for adult, baby in young:
        rows.append((
            f"What is a young {adult} called?",
            baby,
            f"This is the usual name for a newly born or very young {adult}.",
        ))
    groups = [
        ("lions", "pride"), ("wolves", "pack"), ("fish", "school"), ("birds", "flock"),
        ("cattle", "herd"), ("geese", "gaggle"), ("crows", "murder"), ("ants", "colony"),
        ("bees", "swarm"), ("whales", "pod"), ("elephants", "herd"), ("sheep", "flock"),
        ("dolphins", "pod"), ("owls", "parliament"), ("ravens", "unkindness"),
        ("flamingos", "flamboyance"), ("jellyfish", "smack"), ("starlings", "murmuration"),
        ("kangaroos", "mob"), ("hyenas", "clan"),
    ]
    for animal, name in groups:
        rows.append((
            f"What is a group of {animal} called?",
            name,
            f"This collective noun is the usual English name for a group of {animal}.",
        ))
    notes = ["C", "D", "E", "F", "G", "A", "B"]
    for i, note in enumerate(notes):
        nxt = notes[(i + 1) % 7]
        rows.append((
            f"In the C major scale, which note comes immediately after {note}?",
            nxt,
            "The C major scale runs C D E F G A B and then returns to C.",
        ))
    rainbow = ["red", "orange", "yellow", "green", "blue", "indigo", "violet"]
    for i, color in enumerate(rainbow):
        if i == 0:
            continue
        rows.append((
            f"In a rainbow, which colour comes just after {rainbow[i - 1]}?",
            color,
            "A rainbow runs red, orange, yellow, green, blue, indigo, violet.",
        ))
    months = [
        ("January", "31"), ("February", "28 or 29"), ("March", "31"), ("April", "30"),
        ("May", "31"), ("June", "30"), ("July", "31"), ("August", "31"),
        ("September", "30"), ("October", "31"), ("November", "30"), ("December", "31"),
    ]
    for month, days in months:
        rows.append((
            f"How many days does {month} have in the common calendar?",
            days,
            f"This is the usual length of {month}, with February longer in a leap year.",
        ))
    primes = [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71]
    for i, p in enumerate(primes, start=1):
        rows.append((
            f"What is prime number {i}?",
            str(p),
            "Prime numbers are whole numbers greater than 1 with no divisors other than 1 and themselves.",
        ))
    for n in range(1, 65):
        rows.append((
            f"What is {n} written in binary?",
            bin(n)[2:],
            f"Binary writes {n} using only the digits 0 and 1.",
        ))
    polygons = [
        (3, "triangle"), (4, "quadrilateral"), (5, "pentagon"), (6, "hexagon"),
        (7, "heptagon"), (8, "octagon"), (9, "nonagon"), (10, "decagon"),
        (12, "dodecagon"),
    ]
    for sides, name in polygons:
        rows.append((
            f"What is a polygon with {sides} sides called?",
            name,
            f"The name comes from the number of sides: {sides}.",
        ))
    for n in range(1, 201):
        rows.append((
            f"What is the Roman numeral for {n}?",
            roman(n),
            "Roman numerals use I, V, X, L, C, D, and M.",
        ))
    for n in range(1, 61):
        rows.append((
            f"How many seconds are in {n} minute{'s' if n != 1 else ''}?",
            str(n * 60),
            "One minute is 60 seconds.",
        ))
    for a in range(2, 13):
        for b in range(1, 13):
            rows.append((
                f"What is {a} times {b}?",
                str(a * b),
                "This is ordinary multiplication.",
            ))
    return rows


def practice_rows() -> list[tuple[str, str, str]]:
    rows = []
    greek = [
        ("alpha", "A"), ("beta", "B"), ("gamma", "G"), ("delta", "D"), ("epsilon", "E"),
        ("zeta", "Z"), ("eta", "H"), ("theta", "Th"), ("iota", "I"), ("kappa", "K"),
        ("lambda", "L"), ("mu", "M"), ("nu", "N"), ("xi", "X"), ("omicron", "O"),
        ("pi", "P"), ("rho", "R"), ("sigma", "S"), ("tau", "T"), ("upsilon", "U"),
        ("phi", "Ph"), ("chi", "Ch"), ("psi", "Ps"), ("omega", "O"),
    ]
    # omega and omicron both map poorly as single letters; use spoken names as answers instead
    letters = [
        ("first", "alpha"), ("second", "beta"), ("third", "gamma"), ("fourth", "delta"),
        ("fifth", "epsilon"), ("sixth", "zeta"), ("seventh", "eta"), ("eighth", "theta"),
        ("ninth", "iota"), ("tenth", "kappa"), ("eleventh", "lambda"), ("twelfth", "mu"),
        ("thirteenth", "nu"), ("fourteenth", "xi"), ("fifteenth", "omicron"),
        ("sixteenth", "pi"), ("seventeenth", "rho"), ("eighteenth", "sigma"),
        ("nineteenth", "tau"), ("twentieth", "upsilon"), ("twenty-first", "phi"),
        ("twenty-second", "chi"), ("twenty-third", "psi"), ("twenty-fourth", "omega"),
    ]
    for place, name in letters:
        rows.append((
            f"What is the {place} letter of the Greek alphabet?",
            name,
            "The Greek alphabet begins alpha, beta, gamma and ends with omega.",
        ))
    _ = greek
    myths = [
        ("Zeus", "Greek", "sky and thunder"),
        ("Hera", "Greek", "marriage"),
        ("Poseidon", "Greek", "the sea"),
        ("Athena", "Greek", "wisdom"),
        ("Apollo", "Greek", "music and the sun"),
        ("Artemis", "Greek", "the hunt"),
        ("Ares", "Greek", "war"),
        ("Aphrodite", "Greek", "love"),
        ("Hephaestus", "Greek", "the forge"),
        ("Hermes", "Greek", "messengers"),
        ("Hades", "Greek", "the underworld"),
        ("Demeter", "Greek", "the harvest"),
        ("Dionysus", "Greek", "wine"),
        ("Odin", "Norse", "wisdom and war"),
        ("Thor", "Norse", "thunder"),
        ("Freyja", "Norse", "love and magic"),
        ("Loki", "Norse", "mischief"),
        ("Ra", "Egyptian", "the sun"),
        ("Anubis", "Egyptian", "embalming and the dead"),
        ("Isis", "Egyptian", "magic and motherhood"),
        ("Osiris", "Egyptian", "the afterlife"),
        ("Shiva", "Hindu", "destruction and renewal"),
        ("Vishnu", "Hindu", "preservation"),
        ("Brahma", "Hindu", "creation"),
        ("Lakshmi", "Hindu", "wealth"),
        ("Saraswati", "Hindu", "learning"),
        ("Ganesha", "Hindu", "beginnings and obstacles"),
        ("Hanuman", "Hindu", "devotion and strength"),
    ]
    for name, tradition, domain in myths:
        rows.append((
            f"In {tradition} tradition, who is most closely linked with {domain}?",
            name,
            f"This figure belongs to {tradition} stories and is tied to {domain}.",
        ))
    art = [
        ("Mona Lisa", "Leonardo da Vinci"),
        ("The Starry Night", "Vincent van Gogh"),
        ("The Persistence of Memory", "Salvador Dali"),
        ("Guernica", "Pablo Picasso"),
        ("The Birth of Venus", "Sandro Botticelli"),
        ("Girl with a Pearl Earring", "Johannes Vermeer"),
        ("The Scream", "Edvard Munch"),
        ("Water Lilies", "Claude Monet"),
        ("The Night Watch", "Rembrandt"),
        ("American Gothic", "Grant Wood"),
        ("The Kiss", "Gustav Klimt"),
        ("Campbell's Soup Cans", "Andy Warhol"),
        ("The Son of Man", "Rene Magritte"),
        ("Nighthawks", "Edward Hopper"),
        ("The Great Wave off Kanagawa", "Hokusai"),
    ]
    for work, artist in art:
        rows.append((
            f"Who painted {work}?",
            artist,
            f"{work} is a well-known painting by this artist.",
        ))
    body = [
        ("pumps blood", "heart"),
        ("filters blood to make urine", "kidneys"),
        ("exchanges oxygen and carbon dioxide", "lungs"),
        ("produces bile and detoxifies blood", "liver"),
        ("stores and releases bile", "gall bladder"),
        ("produces insulin", "pancreas"),
        ("controls balance and coordination", "cerebellum"),
        ("lets light into the eye", "pupil"),
        ("focuses light in the eye", "lens"),
        ("carries blood away from the heart", "artery"),
        ("carries blood back toward the heart", "vein"),
        ("connects muscle to bone", "tendon"),
        ("connects bone to bone", "ligament"),
        ("is the longest bone in the human body", "femur"),
        ("is the largest organ of the human body", "skin"),
    ]
    for job, part in body:
        rows.append((
            f"Which body part {job}?",
            part,
            f"In human anatomy, this structure is the one that {job}.",
        ))
    space = [
        ("first human in space", "Yuri Gagarin"),
        ("first human on the Moon", "Neil Armstrong"),
        ("second human on the Moon", "Buzz Aldrin"),
        ("first woman in space", "Valentina Tereshkova"),
        ("command module pilot of Apollo 11", "Michael Collins"),
        ("telescope launched in 1990 that orbits Earth", "Hubble Space Telescope"),
        ("rover that landed on Mars in 2012", "Curiosity"),
        ("rover that landed on Mars in 2021", "Perseverance"),
        ("first artificial satellite", "Sputnik 1"),
        ("agency that ran the Apollo Moon landings", "NASA"),
    ]
    for feat, name in space:
        rows.append((
            f"Who or what is known as the {feat}?",
            name,
            f"This is the usual answer for the {feat}.",
        ))
    code = [
        ("200", "OK"),
        ("201", "Created"),
        ("204", "No Content"),
        ("301", "Moved Permanently"),
        ("302", "Found"),
        ("400", "Bad Request"),
        ("401", "Unauthorized"),
        ("403", "Forbidden"),
        ("404", "Not Found"),
        ("500", "Internal Server Error"),
        ("502", "Bad Gateway"),
        ("503", "Service Unavailable"),
    ]
    for num, meaning in code:
        rows.append((
            f"In HTTP, what does status code {num} mean?",
            meaning,
            f"Status {num} is a standard HTTP response.",
        ))
    grammar = [
        ("a word that names a person, place, or thing", "noun"),
        ("a word that shows an action or a state", "verb"),
        ("a word that describes a noun", "adjective"),
        ("a word that describes a verb", "adverb"),
        ("a word that stands in for a noun", "pronoun"),
        ("a word placed before a noun to show relation", "preposition"),
        ("a word that joins words or clauses", "conjunction"),
        ("a word that shows strong feeling", "interjection"),
    ]
    for job, name in grammar:
        rows.append((
            f"In grammar, what do we call {job}?",
            name,
            f"This part of speech is {job}.",
        ))
    powers = [(n, 2 ** n) for n in range(0, 13)]
    for n, val in powers:
        rows.append((
            f"What is 2 to the power of {n}?",
            str(val),
            "Powers of two double at each step, starting from 2 to the power of 0, which is 1.",
        ))
    for n in range(1, 26):
        rows.append((
            f"What is {n} squared?",
            str(n * n),
            f"Squaring means multiplying {n} by itself.",
        ))
    for n in range(1, 13):
        rows.append((
            f"What is {n} cubed?",
            str(n ** 3),
            f"Cubing means multiplying {n} by itself twice.",
        ))
    for n in range(0, 256):
        rows.append((
            f"What is {n} written in hexadecimal?",
            format(n, "X"),
            "Hexadecimal writes numbers with digits 0-9 and letters A-F.",
        ))
    facts = [1]
    for n in range(1, 11):
        facts.append(facts[-1] * n)
        rows.append((
            f"What is {n} factorial?",
            str(facts[-1]),
            "A factorial is the product of every whole number from 1 up to that number.",
        ))
    for n in range(0, 13):
        rows.append((
            f"What is 10 to the power of {n}?",
            str(10 ** n),
            "Powers of ten add a zero for each step up.",
        ))
    for n in range(0, 80):
        rows.append((
            f"What is {n} written in octal?",
            oct(n)[2:],
            "Octal writes numbers using digits 0 through 7.",
        ))
    return rows


def main() -> None:
    existing = set()
    if EXISTING.exists():
        for q in json.loads(EXISTING.read_text(encoding="utf-8")):
            existing.add(q.get("prompt") or q.get("question") or "")
    banks = {
        "classic": classic_rows(),
        "quick": quick_rows(),
        "practice": practice_rows(),
    }
    all_q = []
    used = set(existing)
    for cat, rows in banks.items():
        # drop any prompt already used by category questions or another mode
        fresh = [r for r in rows if r[0] not in used]
        built = take(cat, fresh, used, 500)
        prompts = [q["prompt"] for q in built]
        if len(set(prompts)) != 500:
            raise SystemExit(f"duplicate prompts in {cat}")
        print(f"{cat}: {len(built)} from a bank of {len(rows)}")
        all_q.extend(built)
    OUT.write_text(json.dumps(all_q, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"wrote {len(all_q)} -> {OUT}")


if __name__ == "__main__":
    main()
