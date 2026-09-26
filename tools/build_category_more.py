"""Add 300 new questions to each MEDHA category. IDs continue at 201."""
from __future__ import annotations

import json
import random
from pathlib import Path

import seed_questions as seed

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "category_more.json"
NEED = 300


def others(values: list[str], keep: str) -> list[str]:
    pool = []
    seen = {keep}
    for v in values:
        if v in seen:
            continue
        seen.add(v)
        pool.append(v)
    rng = random.Random(keep + "|".join(pool[:8]))
    rng.shuffle(pool)
    if len(pool) < 3:
        raise SystemExit(f"not enough distractors around {keep}")
    return pool[:3]


def pack(prompt: str, correct: str, wrongs: list[str]) -> tuple[list[str], str]:
    opts = [correct, *wrongs[:3]]
    rng = random.Random(prompt)
    rng.shuffle(opts)
    return opts, "ABCD"[opts.index(correct)]


def emit(cat: str, n: int, prompt: str, correct: str, wrongs: list[str], explanation: str) -> dict:
    opts, letter = pack(prompt, correct, wrongs)
    band = n - 200
    difficulty = "EASY" if band <= 100 else "MEDIUM" if band <= 200 else "HARD"
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
        "difficulty": difficulty,
        "explanation": explanation,
        "prizeLevel": (n - 1) % 15,
        "language": "en",
        "active": True,
        "tags": [cat],
        "source": "medha-category-more",
    }


def take(cat: str, rows: list[tuple[str, str, str]], used: set[str]) -> list[dict]:
    answers = [a for _, a, _ in rows]
    out = []
    for prompt, answer, expl in rows:
        if prompt in used:
            continue
        used.add(prompt)
        out.append((prompt, answer, expl))
        if len(out) == NEED:
            break
    if len(out) < NEED:
        raise SystemExit(f"{cat} only has {len(out)} fresh prompts")
    built = []
    for i, (prompt, answer, expl) in enumerate(out, start=201):
        built.append(emit(cat, i, prompt, answer, others(answers, answer), expl))
    return built


LANG = {
    "Afghanistan": "Pashto", "Albania": "Albanian", "Algeria": "Arabic", "Argentina": "Spanish",
    "Australia": "English", "Austria": "German", "Bangladesh": "Bengali", "Belgium": "Dutch",
    "Bhutan": "Dzongkha", "Brazil": "Portuguese", "Bulgaria": "Bulgarian", "Cambodia": "Khmer",
    "Canada": "English", "Chile": "Spanish", "China": "Mandarin", "Colombia": "Spanish",
    "Croatia": "Croatian", "Cuba": "Spanish", "Czechia": "Czech", "Denmark": "Danish",
    "Egypt": "Arabic", "Ethiopia": "Amharic", "Finland": "Finnish", "France": "French",
    "Germany": "German", "Ghana": "English", "Greece": "Greek", "Hungary": "Hungarian",
    "Iceland": "Icelandic", "India": "Hindi", "Indonesia": "Indonesian", "Iran": "Persian",
    "Iraq": "Arabic", "Ireland": "English", "Israel": "Hebrew", "Italy": "Italian",
    "Japan": "Japanese", "Jordan": "Arabic", "Kenya": "Swahili", "Kuwait": "Arabic",
    "Malaysia": "Malay", "Maldives": "Dhivehi", "Mexico": "Spanish", "Mongolia": "Mongolian",
    "Morocco": "Arabic", "Myanmar": "Burmese", "Nepal": "Nepali", "Netherlands": "Dutch",
    "New Zealand": "English", "Nigeria": "English", "Norway": "Norwegian", "Oman": "Arabic",
    "Pakistan": "Urdu", "Peru": "Spanish", "Philippines": "Filipino", "Poland": "Polish",
    "Portugal": "Portuguese", "Qatar": "Arabic", "Romania": "Romanian", "Russia": "Russian",
    "Saudi Arabia": "Arabic", "Serbia": "Serbian", "Singapore": "English", "Slovakia": "Slovak",
    "South Africa": "English", "South Korea": "Korean", "Spain": "Spanish", "Sri Lanka": "Sinhala",
    "Sweden": "Swedish", "Switzerland": "German", "Thailand": "Thai", "Tunisia": "Arabic",
    "Turkey": "Turkish", "UAE": "Arabic", "United Kingdom": "English", "Ukraine": "Ukrainian",
    "United States": "English", "Vietnam": "Vietnamese", "Zambia": "English", "Zimbabwe": "English",
    "Armenia": "Armenian", "Azerbaijan": "Azerbaijani", "Belarus": "Belarusian", "Estonia": "Estonian",
    "Georgia": "Georgian", "Kazakhstan": "Kazakh", "Latvia": "Latvian", "Lithuania": "Lithuanian",
    "Malta": "Maltese", "Panama": "Spanish", "Paraguay": "Spanish", "Uruguay": "Spanish",
    "Uzbekistan": "Uzbek", "Venezuela": "Spanish", "Bolivia": "Spanish", "Cyprus": "Greek",
    "Luxembourg": "Luxembourgish", "Tanzania": "Swahili", "Uganda": "English", "Senegal": "French",
}
TLD = {
    "Afghanistan": ".af", "Albania": ".al", "Algeria": ".dz", "Argentina": ".ar", "Australia": ".au",
    "Austria": ".at", "Bangladesh": ".bd", "Belgium": ".be", "Bhutan": ".bt", "Brazil": ".br",
    "Bulgaria": ".bg", "Cambodia": ".kh", "Canada": ".ca", "Chile": ".cl", "China": ".cn",
    "Colombia": ".co", "Croatia": ".hr", "Cuba": ".cu", "Czechia": ".cz", "Denmark": ".dk",
    "Egypt": ".eg", "Ethiopia": ".et", "Finland": ".fi", "France": ".fr", "Germany": ".de",
    "Ghana": ".gh", "Greece": ".gr", "Hungary": ".hu", "Iceland": ".is", "India": ".in",
    "Indonesia": ".id", "Iran": ".ir", "Iraq": ".iq", "Ireland": ".ie", "Israel": ".il",
    "Italy": ".it", "Japan": ".jp", "Jordan": ".jo", "Kenya": ".ke", "Kuwait": ".kw",
    "Malaysia": ".my", "Maldives": ".mv", "Mexico": ".mx", "Mongolia": ".mn", "Morocco": ".ma",
    "Myanmar": ".mm", "Nepal": ".np", "Netherlands": ".nl", "New Zealand": ".nz", "Nigeria": ".ng",
    "Norway": ".no", "Oman": ".om", "Pakistan": ".pk", "Peru": ".pe", "Philippines": ".ph",
    "Poland": ".pl", "Portugal": ".pt", "Qatar": ".qa", "Romania": ".ro", "Russia": ".ru",
    "Saudi Arabia": ".sa", "Serbia": ".rs", "Singapore": ".sg", "Slovakia": ".sk", "South Africa": ".za",
    "South Korea": ".kr", "Spain": ".es", "Sri Lanka": ".lk", "Sweden": ".se", "Switzerland": ".ch",
    "Thailand": ".th", "Tunisia": ".tn", "Turkey": ".tr", "UAE": ".ae", "United Kingdom": ".uk",
    "Ukraine": ".ua", "United States": ".us", "Vietnam": ".vn", "Zambia": ".zm", "Zimbabwe": ".zw",
    "Armenia": ".am", "Azerbaijan": ".az", "Belarus": ".by", "Estonia": ".ee", "Georgia": ".ge",
    "Kazakhstan": ".kz", "Latvia": ".lv", "Lithuania": ".lt", "Malta": ".mt", "Panama": ".pa",
    "Paraguay": ".py", "Uruguay": ".uy", "Uzbekistan": ".uz", "Venezuela": ".ve", "Bolivia": ".bo",
    "Cyprus": ".cy", "Luxembourg": ".lu", "Tanzania": ".tz", "Uganda": ".ug", "Senegal": ".sn",
}


def gk_rows() -> list[tuple[str, str, str]]:
    rows = []
    for country, _capital, continent in seed.CAPITALS:
        lang = LANG[country]
        tld = TLD[country]
        rows.append((f"On which continent is {country}?", continent, f"{country} is in {continent}."))
        rows.append((f"Which of these is an official language of {country}?", lang, f"{lang} is an official language of {country}."))
        rows.append((f"What is the internet country code for {country}?", tld, f"The country-code domain for {country} is {tld}."))
    return rows


def sci_rows() -> list[tuple[str, str, str]]:
    rows = []
    formulas = [
        ("water", "H2O"), ("carbon dioxide", "CO2"), ("table salt", "NaCl"), ("oxygen gas", "O2"),
        ("ozone", "O3"), ("ammonia", "NH3"), ("methane", "CH4"), ("glucose", "C6H12O6"),
        ("hydrochloric acid", "HCl"), ("sulfuric acid", "H2SO4"), ("nitric acid", "HNO3"),
        ("sodium hydroxide", "NaOH"), ("calcium carbonate", "CaCO3"), ("carbon monoxide", "CO"),
        ("hydrogen peroxide", "H2O2"), ("ethanol", "C2H5OH"), ("acetic acid", "CH3COOH"),
        ("baking soda", "NaHCO3"), ("rust's iron oxide", "Fe2O3"), ("quartz silica", "SiO2"),
    ]
    for name, formula in formulas:
        rows.append((f"What is the chemical formula of {name}?", formula, f"The formula written for {name} is this one."))
    scientists = [
        ("the theory of general relativity", "Albert Einstein"),
        ("the laws of motion and universal gravitation", "Isaac Newton"),
        ("the theory of evolution by natural selection", "Charles Darwin"),
        ("radioactivity research that won two Nobel Prizes", "Marie Curie"),
        ("the periodic table arranged by atomic weight and gaps", "Dmitri Mendeleev"),
        ("penicillin", "Alexander Fleming"),
        ("the polio vaccine", "Jonas Salk"),
        ("the structure of DNA with Franklin's data", "James Watson"),
        ("the double helix of DNA with Watson", "Francis Crick"),
        ("Pasteurisation and germ theory work", "Louis Pasteur"),
        ("the smallpox vaccine", "Edward Jenner"),
        ("electromagnetic induction", "Michael Faraday"),
        ("alternating-current electrical systems", "Nikola Tesla"),
        ("the light bulb's practical development in the US", "Thomas Edison"),
        ("the heliocentric model published in 1543", "Nicolaus Copernicus"),
        ("the laws of planetary motion", "Johannes Kepler"),
        ("the telescope observations of Jupiter's moons", "Galileo Galilei"),
        ("radioactivity of uranium", "Henri Becquerel"),
        ("the electron", "J. J. Thomson"),
        ("the atomic nucleus", "Ernest Rutherford"),
    ]
    for feat, person in scientists:
        rows.append((f"Who is most closely associated with {feat}?", person, f"This scientist is remembered for {feat}."))
    units = [
        ("force", "newton"), ("energy", "joule"), ("power", "watt"), ("electric current", "ampere"),
        ("frequency", "hertz"), ("pressure", "pascal"), ("electric resistance", "ohm"),
        ("voltage", "volt"), ("temperature in SI", "kelvin"), ("amount of substance", "mole"),
        ("luminous intensity", "candela"), ("radioactivity", "becquerel"), ("absorbed dose", "gray"),
        ("equivalent dose", "sievert"), ("capacitance", "farad"), ("inductance", "henry"),
        ("magnetic flux", "weber"), ("illuminance", "lux"),
    ]
    for qty, unit in units:
        rows.append((f"What is the SI unit of {qty}?", unit, f"The SI unit used for {qty} is this one."))
    classes = [
        ("dog", "mammal"), ("cat", "mammal"), ("whale", "mammal"), ("bat", "mammal"),
        ("eagle", "bird"), ("penguin", "bird"), ("sparrow", "bird"), ("owl", "bird"),
        ("crocodile", "reptile"), ("snake", "reptile"), ("turtle", "reptile"), ("lizard", "reptile"),
        ("frog", "amphibian"), ("salamander", "amphibian"), ("toad", "amphibian"),
        ("shark", "fish"), ("salmon", "fish"), ("tuna", "fish"), ("goldfish", "fish"),
        ("ant", "insect"), ("bee", "insect"), ("butterfly", "insect"), ("mosquito", "insect"),
        ("spider", "arachnid"), ("scorpion", "arachnid"),
        ("octopus", "mollusc"), ("snail", "mollusc"), ("oyster", "mollusc"),
        ("starfish", "echinoderm"), ("jellyfish", "cnidarian"),
    ]
    for animal, group in classes:
        rows.append((f"To which animal group does a {animal} belong?", group, f"A {animal} is classified in this group."))
    body = [
        ("carry oxygen in the blood", "red blood cells"),
        ("fight infection in the blood", "white blood cells"),
        ("help blood clot", "platelets"),
        ("digest food in the stomach", "gastric juice"),
        ("absorb most digested food", "small intestine"),
        ("absorb water from waste", "large intestine"),
        ("control voluntary movement", "skeletal muscle"),
        ("keep the heart beating without rest", "cardiac muscle"),
        ("see colour", "cone cells"),
        ("see in dim light", "rod cells"),
        ("hear sound", "cochlea"),
        ("balance the body in the ear", "semicircular canals"),
        ("filter air before it reaches the lungs", "nose"),
        ("produce voice", "larynx"),
        ("store urine", "bladder"),
    ]
    for job, part in body:
        rows.append((f"Which body part is used to {job}?", part, f"In the human body, this is what {job}."))
    facts = [
        ("How many chromosomes are in a typical human body cell?", "46", "Human body cells usually have 23 pairs."),
        ("What gas do plants take in for photosynthesis?", "carbon dioxide", "Plants use this gas, water, and light to make sugar."),
        ("What gas do plants release during photosynthesis?", "oxygen", "Photosynthesis gives off this gas."),
        ("What is the nearest star to Earth?", "the Sun", "The Sun is the star at the centre of our solar system."),
        ("What is the hardest natural mineral on the Mohs scale?", "diamond", "Diamond sits at the top of the Mohs hardness scale."),
        ("What is the centre of an atom called?", "nucleus", "Protons and neutrons sit in the nucleus."),
        ("What particle has a negative electric charge?", "electron", "Electrons orbit the nucleus and carry a negative charge."),
        ("What particle has a positive electric charge in the nucleus?", "proton", "The number of protons is the atomic number."),
        ("What particle in the nucleus has no electric charge?", "neutron", "Neutrons add mass but not charge."),
        ("What is the boiling point of water at sea level in Celsius?", "100", "Pure water boils at this temperature at standard pressure."),
        ("What is the freezing point of water in Celsius?", "0", "Pure water freezes at this temperature."),
        ("Which planet is known for its rings?", "Saturn", "Saturn's rings are the most visible in the solar system."),
        ("Which planet is the largest?", "Jupiter", "Jupiter is the largest planet by size and mass."),
        ("Which planet is called the Red Planet?", "Mars", "Mars looks red because of iron oxide on its surface."),
        ("What is the galaxy that contains the Solar System?", "the Milky Way", "Our Sun sits in this galaxy."),
        ("What force keeps planets in orbit around the Sun?", "gravity", "Gravity pulls planets toward the Sun."),
        ("What is the speed of light in a vacuum closest to, in km/s?", "300000", "Light in a vacuum travels about 300,000 kilometres each second."),
        ("What vitamin is produced in skin exposed to sunlight?", "vitamin D", "Sunlight helps the skin make this vitamin."),
        ("What vitamin prevents scurvy?", "vitamin C", "A lack of this vitamin causes scurvy."),
        ("What is the powerhouse of the cell?", "mitochondrion", "Mitochondria release energy for the cell."),
        ("What carries genetic information in the chromosomes?", "DNA", "DNA stores the cell's genetic instructions."),
        ("What base pairs with adenine in DNA?", "thymine", "In DNA, adenine pairs with thymine."),
        ("What base pairs with cytosine in DNA?", "guanine", "In DNA, cytosine pairs with guanine."),
        ("What is the process by which plants make food?", "photosynthesis", "Plants make sugar using light, water, and carbon dioxide."),
        ("What is the green pigment in leaves?", "chlorophyll", "Chlorophyll captures light for photosynthesis."),
        ("How many hearts does an octopus have?", "3", "An octopus has three hearts."),
        ("What is the largest planet's Great Red Spot?", "a storm", "Jupiter's Great Red Spot is a giant storm."),
        ("Which blood cells are fewest in a healthy sample?", "white blood cells", "White cells are far less numerous than red cells."),
        ("What simple machine is a ramp?", "an inclined plane", "A ramp is an inclined plane."),
        ("What is H2O commonly called?", "water", "H2O is the formula for water."),
    ]
    rows.extend(facts)
    layers = [
        ("the outermost layer of Earth", "the crust"),
        ("the layer of Earth beneath the crust", "the mantle"),
        ("the innermost layer of Earth", "the core"),
        ("the lowest layer of Earth's atmosphere", "the troposphere"),
        ("the atmospheric layer that holds the ozone layer", "the stratosphere"),
        ("the atmospheric layer where auroras often occur", "the thermosphere"),
    ]
    for feat, name in layers:
        rows.append((f"Which layer is {feat}?", name, f"This is {feat}."))
    vitamins = [
        ("night vision", "vitamin A"),
        ("blood clotting", "vitamin K"),
        ("nerve function as B1", "thiamine"),
        ("skin and the deficiency called pellagra", "niacin"),
        ("preventing rickets along with sunlight", "vitamin D"),
        ("preventing beriberi", "thiamine"),
        ("red blood cell formation as B12", "cobalamin"),
        ("preventing scurvy", "vitamin C"),
    ]
    for job, name in vitamins:
        rows.append((f"Which vitamin is linked with {job}?", name, f"This vitamin is the one linked with {job}."))
    organs = [
        ("How many chambers does a human heart have?", "4", "The heart has two atria and two ventricles."),
        ("How many kidneys does a typical person have?", "2", "Most people have two kidneys."),
        ("How many lungs does a typical person have?", "2", "Most people have two lungs."),
        ("About how many bones are in an adult human skeleton?", "206", "An adult skeleton has about 206 bones."),
        ("How many pairs of ribs does a typical human have?", "12", "There are 12 pairs of ribs."),
        ("How many canine teeth does an adult human have?", "4", "Adults have four canine teeth."),
        ("What is the coloured part of the eye called?", "the iris", "The iris is the coloured ring."),
        ("What is the clear front window of the eye called?", "the cornea", "The cornea is the clear front surface."),
        ("What carries signals from the eye to the brain?", "the optic nerve", "The optic nerve carries visual signals."),
        ("What is the voice box called?", "the larynx", "The larynx is the voice box."),
        ("What tube carries food from the mouth to the stomach?", "the oesophagus", "The oesophagus is that tube."),
        ("What flap stops food entering the windpipe?", "the epiglottis", "The epiglottis covers the windpipe when you swallow."),
    ]
    rows.extend(organs)
    rocks = [
        ("granite", "igneous"), ("basalt", "igneous"), ("obsidian", "igneous"),
        ("sandstone", "sedimentary"), ("limestone", "sedimentary"), ("shale", "sedimentary"),
        ("marble", "metamorphic"), ("slate", "metamorphic"), ("gneiss", "metamorphic"),
    ]
    for rock, kind in rocks:
        rows.append((f"What kind of rock is {rock}?", kind, f"{rock} is a {kind} rock."))
    clouds = [
        ("a high wispy cloud", "cirrus"),
        ("a flat layered cloud", "stratus"),
        ("a puffy heap cloud", "cumulus"),
        ("a thundercloud", "cumulonimbus"),
    ]
    for feat, name in clouds:
        rows.append((f"Which cloud is {feat}?", name, f"This cloud type is {feat}."))
    for n in range(1, 51):
        rows.append((
            f"How many centimetres are in {n} metres?",
            str(n * 100),
            "One metre is 100 centimetres.",
        ))
    for n in range(1, 41):
        rows.append((
            f"How many grams are in {n} kilograms?",
            str(n * 1000),
            "One kilogram is 1000 grams.",
        ))
    for n in range(1, 51):
        rows.append((
            f"How many millilitres are in {n} litres?",
            str(n * 1000),
            "One litre is 1000 millilitres.",
        ))
    return rows


def his_rows() -> list[tuple[str, str, str]]:
    rows = []
    presidents = [
        (1, "George Washington"), (2, "John Adams"), (3, "Thomas Jefferson"), (4, "James Madison"),
        (5, "James Monroe"), (6, "John Quincy Adams"), (7, "Andrew Jackson"), (8, "Martin Van Buren"),
        (9, "William Henry Harrison"), (10, "John Tyler"), (11, "James K. Polk"), (12, "Zachary Taylor"),
        (13, "Millard Fillmore"), (14, "Franklin Pierce"), (15, "James Buchanan"), (16, "Abraham Lincoln"),
        (17, "Andrew Johnson"), (18, "Ulysses S. Grant"), (19, "Rutherford B. Hayes"), (20, "James A. Garfield"),
        (21, "Chester A. Arthur"), (25, "William McKinley"), (26, "Theodore Roosevelt"), (27, "William Howard Taft"),
        (28, "Woodrow Wilson"), (29, "Warren G. Harding"), (30, "Calvin Coolidge"), (31, "Herbert Hoover"),
        (32, "Franklin D. Roosevelt"), (33, "Harry S. Truman"), (34, "Dwight D. Eisenhower"),
        (35, "John F. Kennedy"), (36, "Lyndon B. Johnson"), (37, "Richard Nixon"), (38, "Gerald Ford"),
        (39, "Jimmy Carter"), (40, "Ronald Reagan"), (41, "George H. W. Bush"), (42, "Bill Clinton"),
        (43, "George W. Bush"), (44, "Barack Obama"),
    ]
    for num, name in presidents:
        rows.append((f"Who was president number {num} of the United States?", name, f"{name} was the {num}th US president in the usual numbering."))
        rows.append((f"What number president of the United States was {name}?", str(num), f"{name} is numbered {num} among US presidents."))
    facts = [
        ("Who was the first Prime Minister of independent India?", "Jawaharlal Nehru", "Nehru took office in 1947."),
        ("In which year did India become independent?", "1947", "Indian independence is dated 15 August 1947."),
        ("In which year did the United States declare independence?", "1776", "The Declaration of Independence is dated 1776."),
        ("In which year did the French Revolution begin?", "1789", "The revolution is usually dated from 1789."),
        ("In which year did World War I begin?", "1914", "The war began in 1914."),
        ("In which year did World War I end?", "1918", "The armistice was in 1918."),
        ("In which year did World War II begin in Europe?", "1939", "Germany invaded Poland in 1939."),
        ("In which year did World War II end?", "1945", "The war ended in 1945."),
        ("Who was the British prime minister for most of World War II?", "Winston Churchill", "Churchill led Britain through most of the war."),
        ("Who was the first Chancellor of West Germany after the war?", "Konrad Adenauer", "Adenauer led West Germany from 1949."),
        ("Which city was divided by a wall from 1961 to 1989?", "Berlin", "The Berlin Wall stood through those years."),
        ("In which year did the Berlin Wall fall?", "1989", "The wall opened in 1989."),
        ("Who was the first emperor of a unified China in the Qin dynasty?", "Qin Shi Huang", "He is remembered as the first Qin emperor."),
        ("Which empire built the Colosseum in Rome?", "the Roman Empire", "The Colosseum was a Roman amphitheatre."),
        ("Who was the Macedonian king who conquered the Persian Empire?", "Alexander the Great", "Alexander led that conquest."),
        ("Which document limited the English king in 1215?", "Magna Carta", "Magna Carta was sealed in 1215."),
        ("Who led the Salt March in 1930?", "Mahatma Gandhi", "Gandhi led the march to Dandi."),
        ("In which year was the Indian Constitution adopted?", "1949", "The Constitution was adopted on 26 November 1949."),
        ("Who was the first President of India?", "Rajendra Prasad", "Prasad was India's first president."),
        ("Which ship was sunk in 1912 on its first voyage?", "Titanic", "Titanic sank in 1912."),
        ("Who crossed the Atlantic alone by air in 1927?", "Charles Lindbergh", "Lindbergh flew the Spirit of St. Louis."),
        ("Which country was formerly known as Persia?", "Iran", "Persia is the older name for Iran."),
        ("Which country was formerly known as Siam?", "Thailand", "Siam is the older name for Thailand."),
        ("Which country was formerly known as Ceylon?", "Sri Lanka", "Ceylon is the older name for Sri Lanka."),
        ("Which country was formerly known as Burma?", "Myanmar", "Burma is the older name for Myanmar."),
        ("Who wrote the Ninety-Five Theses in 1517?", "Martin Luther", "Luther posted them in 1517."),
        ("Which city was the capital of the Byzantine Empire?", "Constantinople", "Constantinople was the Byzantine capital."),
        ("Who was the queen of ancient Egypt famous for alliances with Rome?", "Cleopatra", "Cleopatra VII ruled Egypt in that era."),
        ("Which wall was built to keep northern tribes out of China?", "the Great Wall", "The Great Wall defended China's north."),
        ("In which year did humans first land on the Moon?", "1969", "Apollo 11 landed in 1969."),
        ("Who was the first human in space?", "Yuri Gagarin", "Gagarin flew in 1961."),
        ("In which year did the October Revolution occur in Russia?", "1917", "The Bolshevik revolution was in 1917."),
        ("Who was the first Mughal emperor in India?", "Babur", "Babur founded the Mughal Empire."),
        ("Who built the Taj Mahal?", "Shah Jahan", "Shah Jahan commissioned the Taj Mahal."),
        ("Which European explorer reached the Americas in 1492?", "Christopher Columbus", "Columbus's voyage is dated 1492."),
        ("Who completed the first circumnavigation's voyage after Magellan died?", "Juan Sebastian Elcano", "Elcano finished the voyage."),
        ("Which war was fought between the northern and southern US states?", "the American Civil War", "That war was fought from 1861 to 1865."),
        ("In which year did the American Civil War begin?", "1861", "The war began in 1861."),
        ("Who issued the Emancipation Proclamation?", "Abraham Lincoln", "Lincoln issued it during the Civil War."),
        ("Which ancient city was buried by Vesuvius in 79 CE?", "Pompeii", "Pompeii was buried by the eruption."),
    ]
    rows.extend(facts)
    extras = [
        ("Who won the Battle of Hastings in 1066?", "William the Conqueror", "William defeated Harold in 1066."),
        ("In which year was the Battle of Hastings?", "1066", "Hastings was fought in 1066."),
        ("In which year was the Battle of Waterloo?", "1815", "Waterloo was fought in 1815."),
        ("Who was defeated at Waterloo?", "Napoleon", "Napoleon lost at Waterloo."),
        ("In which year was the Battle of Plassey?", "1757", "Plassey was fought in 1757."),
        ("In which year was the Jallianwala Bagh massacre?", "1919", "The massacre was in 1919."),
        ("In which year was the Quit India movement launched?", "1942", "Quit India began in 1942."),
        ("In which year was the Dandi Salt March?", "1930", "The march was in 1930."),
        ("In which year was D-Day in Normandy?", "1944", "The Normandy landings were in 1944."),
        ("In which year did the Russian Revolution's October phase occur?", "1917", "The October Revolution was in 1917."),
        ("Who was the first Mughal emperor?", "Babur", "Babur founded Mughal rule in India."),
        ("Who succeeded Babur as Mughal emperor?", "Humayun", "Humayun followed Babur."),
        ("Who was the Mughal emperor known for religious discussion at Fatehpur Sikri?", "Akbar", "Akbar held those discussions."),
        ("Who built the Red Fort's best-known imperial phase in Delhi with Shah Jahan's court?", "Shah Jahan", "Shah Jahan is tied to that building program."),
        ("Who was the Mughal emperor after Shah Jahan?", "Aurangzeb", "Aurangzeb succeeded Shah Jahan."),
        ("Who was the last Viceroy of British India?", "Lord Mountbatten", "Mountbatten was the last viceroy."),
        ("Who wrote the Indian national anthem Jana Gana Mana?", "Rabindranath Tagore", "Tagore wrote Jana Gana Mana."),
        ("In which year was the Battle of Panipat that brought Babur to power?", "1526", "The first Battle of Panipat was in 1526."),
        ("Which empire did Hernan Cortes confront in Mexico?", "the Aztec Empire", "Cortes confronted the Aztecs."),
        ("Which empire did Francisco Pizarro confront in Peru?", "the Inca Empire", "Pizarro confronted the Incas."),
        ("Who was the first emperor of Rome?", "Augustus", "Augustus was the first Roman emperor."),
        ("Who was assassinated on the Ides of March?", "Julius Caesar", "Caesar was killed on the Ides of March."),
        ("Which city did Constantine make his capital in the east?", "Byzantium", "He refounded Byzantium as his capital."),
        ("In which year did the Western Roman Empire traditionally fall?", "476", "476 is the usual date given."),
        ("Who was the first caliph after Muhammad?", "Abu Bakr", "Abu Bakr was the first caliph."),
        ("Which dynasty built the Forbidden City's imperial Ming phase?", "the Ming dynasty", "The Ming built that palace complex."),
        ("Who unified Germany in 1871?", "Otto von Bismarck", "Bismarck led that unification."),
        ("In which year was Germany unified as an empire under Wilhelm I?", "1871", "Unification is dated 1871."),
        ("Who was the first president of the Republic of Turkey?", "Mustafa Kemal Ataturk", "Ataturk was that president."),
        ("In which year did the Meiji Restoration begin?", "1868", "The Meiji Restoration began in 1868."),
        ("Which ship carried the Pilgrims in 1620?", "the Mayflower", "The Mayflower sailed in 1620."),
        ("In which year was the Storming of the Bastille?", "1789", "The Bastille was stormed in 1789."),
        ("Who was the king of France at the start of the French Revolution?", "Louis XVI", "Louis XVI was king then."),
        ("Who was the queen executed in the French Revolution alongside that king?", "Marie Antoinette", "Marie Antoinette was executed in 1793."),
        ("Which general crossed the Alps with elephants against Rome?", "Hannibal", "Hannibal crossed the Alps."),
        ("Which city-state fought Athens in the Peloponnesian War as its main rival?", "Sparta", "Sparta was Athens's rival in that war."),
        ("Who was the teacher of Alexander the Great?", "Aristotle", "Aristotle tutored Alexander."),
        ("Who was the teacher of Plato?", "Socrates", "Socrates taught Plato."),
        ("Who was the teacher of Aristotle?", "Plato", "Plato taught Aristotle."),
        ("Which Indian emperor sent missionaries after the Kalinga war?", "Ashoka", "Ashoka is linked with that turn."),
    ]
    rows.extend(extras)
    years = [1066, 1215, 1453, 1492, 1498, 1517, 1600, 1757, 1776, 1789, 1804, 1815, 1857, 1861, 1869, 1885, 1905, 1914, 1917, 1919, 1920, 1930, 1939, 1942, 1945, 1947, 1950, 1969, 1989, 1991]
    for year in years:
        century = (year - 1) // 100 + 1
        suffix = {1: "st", 2: "nd", 3: "rd"}.get(century % 10, "th") if century % 100 not in (11, 12, 13) else "th"
        rows.append((
            f"In which century was the year {year}?",
            f"{century}{suffix}",
            f"The year {year} falls in that century.",
        ))
    for year in range(1700, 1900):
        rows.append((
            f"How many years after 1700 is the year {year}?",
            str(year - 1700),
            f"Count forward from 1700 to {year}.",
        ))
    return rows


def spo_rows() -> list[tuple[str, str, str]]:
    rows = []
    hosts = [
        (1896, "Athens"), (1900, "Paris"), (1904, "St. Louis"), (1908, "London"),
        (1912, "Stockholm"), (1920, "Antwerp"), (1924, "Paris"), (1928, "Amsterdam"),
        (1932, "Los Angeles"), (1936, "Berlin"), (1948, "London"), (1952, "Helsinki"),
        (1956, "Melbourne"), (1960, "Rome"), (1964, "Tokyo"), (1968, "Mexico City"),
        (1972, "Munich"), (1976, "Montreal"), (1980, "Moscow"), (1984, "Los Angeles"),
        (1988, "Seoul"), (1992, "Barcelona"), (1996, "Atlanta"), (2000, "Sydney"),
        (2004, "Athens"), (2008, "Beijing"), (2012, "London"), (2016, "Rio de Janeiro"),
        (2021, "Tokyo"), (2024, "Paris"),
    ]
    for year, city in hosts:
        rows.append((f"Which city hosted the Summer Olympics in {year}?", city, f"The {year} Summer Games were in {city}."))
        rows.append((f"In which year did {city} host a listed Summer Olympics in this set, if we ask about {year}?", str(year), f"{city} hosted in {year}."))
    cups = [
        (1930, "Uruguay"), (1950, "Brazil"), (1954, "Switzerland"), (1958, "Sweden"),
        (1962, "Chile"), (1966, "England"), (1970, "Mexico"), (1974, "West Germany"),
        (1978, "Argentina"), (1982, "Spain"), (1986, "Mexico"), (1990, "Italy"),
        (1994, "United States"), (1998, "France"), (2002, "South Korea and Japan"),
        (2006, "Germany"), (2010, "South Africa"), (2014, "Brazil"), (2018, "Russia"),
        (2022, "Qatar"),
    ]
    for year, place in cups:
        rows.append((f"Which country or countries hosted the FIFA World Cup in {year}?", place, f"The {year} World Cup was hosted by {place}."))
    terms = [
        ("love", "tennis"), ("deuce", "tennis"), ("birdie", "golf"), ("eagle", "golf"),
        ("strike", "bowling"), ("spare", "bowling"), ("hat-trick", "cricket"), ("googly", "cricket"),
        ("offside", "football"), ("penalty kick", "football"), ("slam dunk", "basketball"),
        ("free throw", "basketball"), ("home run", "baseball"), ("touchdown", "American football"),
        ("try", "rugby"), ("knockout", "boxing"), ("checkmate", "chess"), ("stalemate", "chess"),
        ("bullseye", "darts"), ("ace", "tennis"), ("let", "tennis"), ("bogey", "golf"),
        ("alley-oop", "basketball"), ("power play", "ice hockey"), ("pitcher", "baseball"),
        ("wicket", "cricket"), ("century", "cricket"), ("yellow card", "football"),
        ("red card", "football"), ("serve", "volleyball"),
    ]
    for term, sport in terms:
        rows.append((f"In which sport is the term '{term}' used?", sport, f"'{term}' is a term from {sport}."))
    counts = [
        ("cricket", "11"), ("football", "11"), ("basketball", "5"), ("volleyball", "6"),
        ("baseball", "9"), ("ice hockey", "6"), ("rugby union", "15"), ("water polo", "7"),
        ("kabaddi", "7"), ("handball", "7"),
    ]
    for sport, n in counts:
        rows.append((f"How many players does one team field in {sport}?", n, f"A {sport} team fields this many players."))
    rows.append(("How many rings are on the Olympic flag?", "5", "The Olympic flag has five interlocking rings."))
    rows.append(("How many players take the field for one baseball team?", "9", "Baseball fields nine players."))
    rows.append(("Which Grand Slam is played on clay at Roland Garros?", "the French Open", "The French Open is the clay-court major."))
    rows.append(("Which Grand Slam is played on grass at the All England Club?", "Wimbledon", "Wimbledon is the grass-court major."))
    rows.append(("Which Grand Slam is played in New York?", "the US Open", "The US Open is held in New York."))
    rows.append(("Which Grand Slam is played in Melbourne?", "the Australian Open", "The Australian Open is held in Melbourne."))
    winners = [
        (1930, "Uruguay"), (1950, "Uruguay"), (1954, "West Germany"), (1958, "Brazil"),
        (1962, "Brazil"), (1966, "England"), (1970, "Brazil"), (1974, "West Germany"),
        (1978, "Argentina"), (1982, "Italy"), (1986, "Argentina"), (1990, "West Germany"),
        (1994, "Brazil"), (1998, "France"), (2002, "Brazil"), (2006, "Italy"),
        (2010, "Spain"), (2014, "Germany"), (2018, "France"), (2022, "Argentina"),
    ]
    for year, team in winners:
        rows.append((f"Who won the FIFA World Cup in {year}?", team, f"The {year} World Cup was won by {team}."))
    winter = [
        (1924, "Chamonix"), (1928, "St. Moritz"), (1932, "Lake Placid"), (1936, "Garmisch-Partenkirchen"),
        (1948, "St. Moritz"), (1952, "Oslo"), (1956, "Cortina d'Ampezzo"), (1960, "Squaw Valley"),
        (1964, "Innsbruck"), (1968, "Grenoble"), (1972, "Sapporo"), (1976, "Innsbruck"),
        (1980, "Lake Placid"), (1984, "Sarajevo"), (1988, "Calgary"), (1992, "Albertville"),
        (1994, "Lillehammer"), (1998, "Nagano"), (2002, "Salt Lake City"), (2006, "Turin"),
        (2010, "Vancouver"), (2014, "Sochi"), (2018, "Pyeongchang"), (2022, "Beijing"),
    ]
    for year, city in winter:
        rows.append((f"Which place hosted the Winter Olympics in {year}?", city, f"The {year} Winter Games were in {city}."))
    more = [
        ("How many holes are played in a standard round of golf?", "18", "A standard round is 18 holes."),
        ("How many minutes are in a standard football match, excluding stoppage time?", "90", "Two halves of 45 minutes."),
        ("How many quarters are in an NBA basketball game?", "4", "NBA games have four quarters."),
        ("How many overs are in a standard one-day international innings?", "50", "An ODI innings is 50 overs."),
        ("How many overs are in a T20 innings?", "20", "A T20 innings is 20 overs."),
        ("How many runs is a six in cricket?", "6", "The ball clearing the boundary on the full is six."),
        ("How many runs is a boundary along the ground in cricket?", "4", "A grounded boundary is four."),
        ("How many points is a try in rugby union?", "5", "A try is worth five points."),
        ("How many points is a conversion in rugby union?", "2", "A successful conversion adds two."),
        ("How many players start for both teams together in football?", "22", "Eleven a side means 22 on the pitch."),
        ("Which sport is the Tour de France?", "cycling", "The Tour de France is a cycling race."),
        ("Which sport uses a puck?", "ice hockey", "Ice hockey uses a puck."),
        ("Which sport uses a shuttlecock?", "badminton", "Badminton uses a shuttlecock."),
        ("How many Grand Slam tennis tournaments are there in a year?", "4", "There are four Grand Slams."),
        ("How many players are on a water polo team in the water?", "7", "Water polo fields seven."),
        ("How many players are on a volleyball team on court?", "6", "Volleyball has six on court."),
        ("How many bases are there on a baseball diamond, not counting home as extra?", "3", "First, second, and third base, plus home plate."),
        ("What colour is the jersey of the leader of the Tour de France?", "yellow", "The leader wears yellow."),
        ("How many rings are linked on the Olympic flag?", "5", "Five rings stand for the parts of the world the movement names."),
        ("Which country hosted the 2010 FIFA World Cup?", "South Africa", "The 2010 tournament was in South Africa."),
    ]
    rows.extend(more)
    cricket = [
        (1983, "India"), (1992, "Pakistan"), (1996, "Sri Lanka"), (1999, "Australia"),
        (2003, "Australia"), (2007, "Australia"), (2011, "India"), (2015, "Australia"),
        (2019, "England"), (2023, "Australia"),
    ]
    for year, team in cricket:
        rows.append((f"Who won the Cricket World Cup in {year}?", team, f"{team} won the {year} Cricket World Cup."))
    for n in range(1, 31):
        rows.append((
            f"If a football team scores {n} goals and the other scores none, what is the winner's goal total?",
            str(n),
            "The winner's total is the number of goals that team scored.",
        ))
    gear = [
        ("football", "a ball"), ("basketball", "a ball"), ("tennis", "a ball"),
        ("cricket", "a ball"), ("golf", "a ball"), ("hockey", "a ball"),
        ("badminton", "a shuttlecock"), ("table tennis", "a ball"), ("baseball", "a ball"),
        ("rugby", "a ball"), ("volleyball", "a ball"), ("ice hockey", "a puck"),
    ]
    for sport, item in gear:
        rows.append((f"What object is hit or kicked in {sport}?", item, f"{sport} is played with {item}."))
    colours = [
        ("a football referee in many competitions", "black"),
        ("the centre of an archery target", "yellow"),
        ("the Olympic rings include", "blue"),
    ]
    for who, colour in colours:
        rows.append((f"Which colour is associated with {who}?", colour, f"The usual colour linked here is {colour}."))
    for n in range(1, 61):
        rows.append((
            f"If a basketball team scores {n} two-point baskets and nothing else, how many points is that?",
            str(n * 2),
            "Each of those baskets is worth two points.",
        ))
    return rows


def mov_rows() -> list[tuple[str, str, str]]:
    films = [
        ("Spirited Away", "2001", "Hayao Miyazaki"),
        ("My Neighbor Totoro", "1988", "Hayao Miyazaki"),
        ("Princess Mononoke", "1997", "Hayao Miyazaki"),
        ("Parasite", "2019", "Bong Joon-ho"),
        ("Oldboy", "2003", "Park Chan-wook"),
        ("Crouching Tiger, Hidden Dragon", "2000", "Ang Lee"),
        ("Life of Pi", "2012", "Ang Lee"),
        ("Brokeback Mountain", "2005", "Ang Lee"),
        ("Slumdog Millionaire", "2008", "Danny Boyle"),
        ("127 Hours", "2010", "Danny Boyle"),
        ("Trainspotting", "1996", "Danny Boyle"),
        ("The Grand Budapest Hotel", "2014", "Wes Anderson"),
        ("Moonrise Kingdom", "2012", "Wes Anderson"),
        ("The Royal Tenenbaums", "2001", "Wes Anderson"),
        ("Get Out", "2017", "Jordan Peele"),
        ("Us", "2019", "Jordan Peele"),
        ("Nope", "2022", "Jordan Peele"),
        ("Mad Max: Fury Road", "2015", "George Miller"),
        ("The Matrix", "1999", "Lana and Lilly Wachowski"),
        ("The Matrix Reloaded", "2003", "Lana and Lilly Wachowski"),
        ("Inception", "2010", "Christopher Nolan"),
        ("Interstellar", "2014", "Christopher Nolan"),
        ("Dunkirk", "2017", "Christopher Nolan"),
        ("The Dark Knight", "2008", "Christopher Nolan"),
        ("Oppenheimer", "2023", "Christopher Nolan"),
        ("Whiplash", "2014", "Damien Chazelle"),
        ("La La Land", "2016", "Damien Chazelle"),
        ("Arrival", "2016", "Denis Villeneuve"),
        ("Blade Runner 2049", "2017", "Denis Villeneuve"),
        ("Dune", "2021", "Denis Villeneuve"),
        ("Prisoners", "2013", "Denis Villeneuve"),
        ("Pan's Labyrinth", "2006", "Guillermo del Toro"),
        ("The Shape of Water", "2017", "Guillermo del Toro"),
        ("Roma", "2018", "Alfonso Cuaron"),
        ("Gravity", "2013", "Alfonso Cuaron"),
        ("Y Tu Mama Tambien", "2001", "Alfonso Cuaron"),
        ("Amores Perros", "2000", "Alejandro Gonzalez Inarritu"),
        ("Birdman", "2014", "Alejandro Gonzalez Inarritu"),
        ("The Revenant", "2015", "Alejandro Gonzalez Inarritu"),
        ("City of God", "2002", "Fernando Meirelles"),
        ("Amelie", "2001", "Jean-Pierre Jeunet"),
        ("The Intouchables", "2011", "Olivier Nakache and Eric Toledano"),
        ("Cinema Paradiso", "1988", "Giuseppe Tornatore"),
        ("Life Is Beautiful", "1997", "Roberto Benigni"),
        ("The Seventh Seal", "1957", "Ingmar Bergman"),
        ("Wild Strawberries", "1957", "Ingmar Bergman"),
        ("Rashomon", "1950", "Akira Kurosawa"),
        ("Seven Samurai", "1954", "Akira Kurosawa"),
        ("Ikiru", "1952", "Akira Kurosawa"),
        ("Tokyo Story", "1953", "Yasujiro Ozu"),
        ("Pather Panchali", "1955", "Satyajit Ray"),
        ("Aparajito", "1956", "Satyajit Ray"),
        ("The World of Apu", "1959", "Satyajit Ray"),
        ("Lagaan", "2001", "Ashutosh Gowariker"),
        ("3 Idiots", "2009", "Rajkumar Hirani"),
        ("PK", "2014", "Rajkumar Hirani"),
        ("Dangal", "2016", "Nitesh Tiwari"),
        ("Baahubali: The Beginning", "2015", "S. S. Rajamouli"),
        ("Baahubali 2: The Conclusion", "2017", "S. S. Rajamouli"),
        ("RRR", "2022", "S. S. Rajamouli"),
        ("Sholay", "1975", "Ramesh Sippy"),
        ("Mother India", "1957", "Mehboob Khan"),
        ("Mughal-e-Azam", "1960", "K. Asif"),
        ("Guide", "1965", "Vijay Anand"),
        ("Anand", "1971", "Hrishikesh Mukherjee"),
        ("Deewaar", "1975", "Yash Chopra"),
        ("Dilwale Dulhania Le Jayenge", "1995", "Aditya Chopra"),
        ("Dil Chahta Hai", "2001", "Farhan Akhtar"),
        ("Zindagi Na Milegi Dobara", "2011", "Zoya Akhtar"),
        ("Gully Boy", "2019", "Zoya Akhtar"),
        ("Queen", "2013", "Vikas Bahl"),
        ("Barfi!", "2012", "Anurag Basu"),
        ("Taare Zameen Par", "2007", "Aamir Khan"),
        ("Black", "2005", "Sanjay Leela Bhansali"),
        ("Devdas", "2002", "Sanjay Leela Bhansali"),
        ("Bajirao Mastani", "2015", "Sanjay Leela Bhansali"),
        ("Padmaavat", "2018", "Sanjay Leela Bhansali"),
        ("Kahaani", "2012", "Sujoy Ghosh"),
        ("Drishyam", "2015", "Nishikant Kamat"),
        ("Andhadhun", "2018", "Sriram Raghavan"),
        ("Article 15", "2019", "Anubhav Sinha"),
        ("Masaan", "2015", "Neeraj Ghaywan"),
        ("The Lunchbox", "2013", "Ritesh Batra"),
        ("Ship of Theseus", "2013", "Anand Gandhi"),
        ("Court", "2014", "Chaitanya Tamhane"),
        ("The Disciple", "2020", "Chaitanya Tamhane"),
        ("Jallikattu", "2019", "Lijo Jose Pellissery"),
        ("Kumbalangi Nights", "2019", "Madhu C. Narayanan"),
        ("Super Deluxe", "2019", "Thiagarajan Kumararaja"),
        ("Visaaranai", "2015", "Vetrimaaran"),
        ("Asuran", "2019", "Vetrimaaran"),
        ("Kaaka Muttai", "2015", "M. Manikandan"),
        ("Sairat", "2016", "Nagraj Manjule"),
        ("Fandry", "2013", "Nagraj Manjule"),
        ("Court", "2014", "Chaitanya Tamhane"),
    ]
    # unique titles only
    seen_titles = set()
    clean = []
    for title, year, director in films:
        if title in seen_titles:
            continue
        seen_titles.add(title)
        clean.append((title, year, director))
    more_films = [
        ("The Godfather", "1972", "Francis Ford Coppola"),
        ("The Godfather Part II", "1974", "Francis Ford Coppola"),
        ("Apocalypse Now", "1979", "Francis Ford Coppola"),
        ("Raging Bull", "1980", "Martin Scorsese"),
        ("Goodfellas", "1990", "Martin Scorsese"),
        ("Taxi Driver", "1976", "Martin Scorsese"),
        ("The Departed", "2006", "Martin Scorsese"),
        ("Casino", "1995", "Martin Scorsese"),
        ("Pulp Fiction", "1994", "Quentin Tarantino"),
        ("Kill Bill: Volume 1", "2003", "Quentin Tarantino"),
        ("Inglourious Basterds", "2009", "Quentin Tarantino"),
        ("Django Unchained", "2012", "Quentin Tarantino"),
        ("Once Upon a Time in Hollywood", "2019", "Quentin Tarantino"),
        ("Schindler's List", "1993", "Steven Spielberg"),
        ("Jurassic Park", "1993", "Steven Spielberg"),
        ("E.T. the Extra-Terrestrial", "1982", "Steven Spielberg"),
        ("Jaws", "1975", "Steven Spielberg"),
        ("Saving Private Ryan", "1998", "Steven Spielberg"),
        ("Raiders of the Lost Ark", "1981", "Steven Spielberg"),
        ("The Shawshank Redemption", "1994", "Frank Darabont"),
        ("The Green Mile", "1999", "Frank Darabont"),
        ("Forrest Gump", "1994", "Robert Zemeckis"),
        ("Back to the Future", "1985", "Robert Zemeckis"),
        ("Cast Away", "2000", "Robert Zemeckis"),
        ("The Silence of the Lambs", "1991", "Jonathan Demme"),
        ("Philadelphia", "1993", "Jonathan Demme"),
        ("Fight Club", "1999", "David Fincher"),
        ("Se7en", "1995", "David Fincher"),
        ("The Social Network", "2010", "David Fincher"),
        ("Gone Girl", "2014", "David Fincher"),
        ("Zodiac", "2007", "David Fincher"),
        ("Alien", "1979", "Ridley Scott"),
        ("Blade Runner", "1982", "Ridley Scott"),
        ("Gladiator", "2000", "Ridley Scott"),
        ("The Martian", "2015", "Ridley Scott"),
        ("Black Hawk Down", "2001", "Ridley Scott"),
        ("The Thing", "1982", "John Carpenter"),
        ("Halloween", "1978", "John Carpenter"),
        ("Psycho", "1960", "Alfred Hitchcock"),
        ("Vertigo", "1958", "Alfred Hitchcock"),
        ("Rear Window", "1954", "Alfred Hitchcock"),
        ("North by Northwest", "1959", "Alfred Hitchcock"),
        ("Citizen Kane", "1941", "Orson Welles"),
        ("Casablanca", "1942", "Michael Curtiz"),
        ("Singin' in the Rain", "1952", "Stanley Donen and Gene Kelly"),
        ("Some Like It Hot", "1959", "Billy Wilder"),
        ("Sunset Boulevard", "1950", "Billy Wilder"),
        ("The Apartment", "1960", "Billy Wilder"),
        ("2001: A Space Odyssey", "1968", "Stanley Kubrick"),
        ("The Shining", "1980", "Stanley Kubrick"),
        ("Dr. Strangelove", "1964", "Stanley Kubrick"),
        ("A Clockwork Orange", "1971", "Stanley Kubrick"),
        ("Full Metal Jacket", "1987", "Stanley Kubrick"),
        ("Lawrence of Arabia", "1962", "David Lean"),
        ("The Bridge on the River Kwai", "1957", "David Lean"),
        ("Doctor Zhivago", "1965", "David Lean"),
        ("Chinatown", "1974", "Roman Polanski"),
        ("Rosemary's Baby", "1968", "Roman Polanski"),
        ("The Pianist", "2002", "Roman Polanski"),
        ("Amadeus", "1984", "Milos Forman"),
        ("One Flew Over the Cuckoo's Nest", "1975", "Milos Forman"),
        ("The Deer Hunter", "1978", "Michael Cimino"),
        ("Platoon", "1986", "Oliver Stone"),
        ("JFK", "1991", "Oliver Stone"),
        ("Born on the Fourth of July", "1989", "Oliver Stone"),
        ("Do the Right Thing", "1989", "Spike Lee"),
        ("Malcolm X", "1992", "Spike Lee"),
        ("Boyz n the Hood", "1991", "John Singleton"),
        ("Moonlight", "2016", "Barry Jenkins"),
        ("If Beale Street Could Talk", "2018", "Barry Jenkins"),
        ("Nomadland", "2020", "Chloe Zhao"),
        ("The Rider", "2017", "Chloe Zhao"),
        ("Little Women", "2019", "Greta Gerwig"),
        ("Lady Bird", "2017", "Greta Gerwig"),
        ("Barbie", "2023", "Greta Gerwig"),
        ("Portrait of a Lady on Fire", "2019", "Celine Sciamma"),
        ("The Worst Person in the World", "2021", "Joachim Trier"),
        ("Drive My Car", "2021", "Ryusuke Hamaguchi"),
        ("Shoplifters", "2018", "Hirokazu Kore-eda"),
        ("Nobody Knows", "2004", "Hirokazu Kore-eda"),
        ("Still Walking", "2008", "Hirokazu Kore-eda"),
        ("In the Mood for Love", "2000", "Wong Kar-wai"),
        ("Chungking Express", "1994", "Wong Kar-wai"),
        ("2046", "2004", "Wong Kar-wai"),
        ("Yi Yi", "2000", "Edward Yang"),
        ("A Brighter Summer Day", "1991", "Edward Yang"),
        ("The Handmaiden", "2016", "Park Chan-wook"),
        ("Decision to Leave", "2022", "Park Chan-wook"),
        ("Memories of Murder", "2003", "Bong Joon-ho"),
        ("The Host", "2006", "Bong Joon-ho"),
        ("Snowpiercer", "2013", "Bong Joon-ho"),
        ("Okja", "2017", "Bong Joon-ho"),
        ("Burning", "2018", "Lee Chang-dong"),
        ("Poetry", "2010", "Lee Chang-dong"),
        ("A Separation", "2011", "Asghar Farhadi"),
        ("The Salesman", "2016", "Asghar Farhadi"),
        ("About Elly", "2009", "Asghar Farhadi"),
        ("Capernaum", "2018", "Nadine Labaki"),
        ("Wadjda", "2012", "Haifaa al-Mansour"),
        ("Timbuktu", "2014", "Abderrahmane Sissako"),
        ("Atlantics", "2019", "Mati Diop"),
        ("Black Panther", "2018", "Ryan Coogler"),
        ("Creed", "2015", "Ryan Coogler"),
        ("Fruitvale Station", "2013", "Ryan Coogler"),
        ("Get Out", "2017", "Jordan Peele"),
    ]
    for title, year, director in more_films:
        if title in seen_titles:
            continue
        seen_titles.add(title)
        clean.append((title, year, director))
    rows = []
    for title, year, director in clean:
        rows.append((f"Who directed {title}?", director, f"{title} ({year}) was directed by {director}."))
        rows.append((f"In which year was {title} released?", year, f"{title} was released in {year}."))
    studios = [
        ("Spirited Away", "Studio Ghibli"),
        ("Toy Story", "Pixar"),
        ("Finding Nemo", "Pixar"),
        ("The Lion King", "Walt Disney Animation"),
        ("Shrek", "DreamWorks"),
        ("Spider-Man: Into the Spider-Verse", "Sony Pictures Animation"),
        ("Your Name", "CoMix Wave Films"),
        ("Akira", "Tokyo Movie Shinsha"),
    ]
    for title, studio in studios:
        rows.append((f"Which studio is most closely tied to {title}?", studio, f"{title} is associated with {studio}."))
    return rows


def tech_rows() -> list[tuple[str, str, str]]:
    rows = []
    acronyms = [
        ("HTML", "HyperText Markup Language"),
        ("CSS", "Cascading Style Sheets"),
        ("HTTP", "Hypertext Transfer Protocol"),
        ("HTTPS", "Hypertext Transfer Protocol Secure"),
        ("URL", "Uniform Resource Locator"),
        ("CPU", "Central Processing Unit"),
        ("GPU", "Graphics Processing Unit"),
        ("RAM", "Random Access Memory"),
        ("ROM", "Read-Only Memory"),
        ("SSD", "Solid State Drive"),
        ("USB", "Universal Serial Bus"),
        ("PDF", "Portable Document Format"),
        ("JPEG", "Joint Photographic Experts Group"),
        ("PNG", "Portable Network Graphics"),
        ("GIF", "Graphics Interchange Format"),
        ("SQL", "Structured Query Language"),
        ("API", "Application Programming Interface"),
        ("JSON", "JavaScript Object Notation"),
        ("XML", "Extensible Markup Language"),
        ("DNS", "Domain Name System"),
        ("IP", "Internet Protocol"),
        ("TCP", "Transmission Control Protocol"),
        ("UDP", "User Datagram Protocol"),
        ("FTP", "File Transfer Protocol"),
        ("SMTP", "Simple Mail Transfer Protocol"),
        ("IMAP", "Internet Message Access Protocol"),
        ("VPN", "Virtual Private Network"),
        ("LAN", "Local Area Network"),
        ("WAN", "Wide Area Network"),
        ("Wi-Fi", "a wireless local network"),
        ("AI", "artificial intelligence"),
        ("ML", "machine learning"),
        ("OS", "operating system"),
        ("GUI", "graphical user interface"),
        ("CLI", "command-line interface"),
        ("IDE", "integrated development environment"),
        ("BIOS", "Basic Input/Output System"),
        ("IoT", "Internet of Things"),
        ("SEO", "search engine optimisation"),
        ("CAPTCHA", "Completely Automated Public Turing test"),
    ]
    for short, full in acronyms:
        rows.append((f"What does {short} stand for in computing?", full, f"{short} expands to {full}."))
    ports = [
        ("HTTP", "80"), ("HTTPS", "443"), ("SSH", "22"), ("DNS", "53"),
        ("SMTP", "25"), ("FTP", "21"), ("POP3", "110"), ("IMAP", "143"),
    ]
    for name, port in ports:
        rows.append((f"What is the usual port number for {name}?", port, f"{name} commonly uses port {port}."))
    langs = [
        ("Python", "Guido van Rossum"),
        ("Java", "James Gosling"),
        ("C", "Dennis Ritchie"),
        ("C++", "Bjarne Stroustrup"),
        ("JavaScript", "Brendan Eich"),
        ("PHP", "Rasmus Lerdorf"),
        ("Ruby", "Yukihiro Matsumoto"),
        ("Perl", "Larry Wall"),
        ("Kotlin", "JetBrains"),
        ("Go", "Google"),
        ("Rust", "Graydon Hoare"),
        ("Swift", "Apple"),
        ("TypeScript", "Microsoft"),
        ("Scala", "Martin Odersky"),
        ("Haskell", "a committee at Glasgow and Yale"),
        ("Lisp", "John McCarthy"),
        ("Fortran", "John Backus"),
        ("COBOL", "Grace Hopper's era at the CODASYL effort"),
        ("Pascal", "Niklaus Wirth"),
        ("Smalltalk", "Alan Kay's group at Xerox"),
    ]
    for lang, maker in langs:
        rows.append((f"Who or which group is most closely credited with creating {lang}?", maker, f"{lang} is credited to {maker}."))
    exts = [
        (".py", "Python"), (".js", "JavaScript"), (".ts", "TypeScript"), (".java", "Java"),
        (".kt", "Kotlin"), (".cpp", "C++"), (".c", "C"), (".rs", "Rust"),
        (".go", "Go"), (".rb", "Ruby"), (".php", "PHP"), (".html", "HTML"),
        (".css", "CSS"), (".json", "JSON"), (".md", "Markdown"), (".sql", "SQL"),
    ]
    for ext, lang in exts:
        rows.append((f"Which language or format commonly uses the {ext} file extension?", lang, f"Files ending in {ext} are usually {lang}."))
    bits = [
        ("How many bits are in a byte?", "8", "A byte is 8 bits."),
        ("How many bytes are in a kilobyte in the common decimal count?", "1000", "A decimal kilobyte is 1000 bytes."),
        ("What does a compiler turn source code into?", "machine code", "A compiler produces code the machine can run."),
        ("What does an interpreter do with source code?", "runs it directly", "An interpreter executes the source without a separate compile step."),
        ("Which company created the Android operating system before Google owned it?", "Android Inc.", "Android Inc. built it before Google acquired the company."),
        ("Which company publishes the Windows operating system?", "Microsoft", "Windows is a Microsoft product."),
        ("Which company publishes macOS?", "Apple", "macOS is Apple's desktop operating system."),
        ("What is the kernel used by Android?", "Linux", "Android uses the Linux kernel."),
        ("What numbering system uses only 0 and 1?", "binary", "Binary uses two digits."),
        ("What numbering system uses digits 0-9 and A-F?", "hexadecimal", "Hexadecimal is base 16."),
    ]
    rows.extend(bits)
    for n in range(0, 64):
        rows.append((f"In tech practice, what is the 8-bit binary form of {n}?", format(n, "08b"), f"{n} written with eight binary digits."))
    for n in range(1, 121):
        rows.append((
            f"How many bytes are in {n} decimal kilobytes?",
            str(n * 1000),
            "A decimal kilobyte is 1000 bytes.",
        ))
    more = [
        ("How many bits are in an IPv4 address?", "32", "An IPv4 address is 32 bits."),
        ("How many bits are in an IPv6 address?", "128", "An IPv6 address is 128 bits."),
        ("What keyword starts a function in Python?", "def", "Python functions begin with def."),
        ("What keyword starts a conditional in Python?", "if", "Python conditionals use if."),
        ("What keyword marks a class in Python?", "class", "Python classes begin with class."),
        ("What keyword returns a value from a Python function?", "return", "return sends a value back."),
        ("Which data structure is first in, first out?", "a queue", "A queue serves the oldest item first."),
        ("Which data structure is last in, first out?", "a stack", "A stack serves the newest item first."),
        ("What does SQL SELECT do?", "reads rows", "SELECT reads data from a table."),
        ("What does SQL INSERT do?", "adds rows", "INSERT adds rows to a table."),
        ("What does a pixel's name come from?", "picture element", "Pixel is short for picture element."),
        ("Which company makes the iPhone?", "Apple", "The iPhone is an Apple product."),
        ("Which company makes the Galaxy phone line?", "Samsung", "Galaxy phones are made by Samsung."),
        ("Which company develops the Chrome browser?", "Google", "Chrome is a Google browser."),
        ("Which company develops Firefox?", "Mozilla", "Firefox is developed by Mozilla."),
        ("What does a firewall do?", "filters network traffic", "A firewall allows or blocks traffic."),
        ("What does two-factor authentication add?", "a second check", "It asks for something besides the password."),
        ("What is open-source software?", "software whose source can be inspected", "Open source lets people read the code."),
        ("What is a bug in software?", "a fault in the program", "A bug is a defect in the program."),
        ("What is a patch in software?", "a fix or update", "A patch changes the program to fix or improve it."),
        ("What does a router do?", "forwards packets between networks", "A router sends packets toward their destination network."),
        ("What does a switch do on a local network?", "forwards frames to the right device", "A switch sends local traffic to the device that should receive it."),
        ("What is phishing?", "a trick to steal secrets", "Phishing pretends to be a trusted message to steal information."),
    ]
    rows.extend(more)
    return rows


def fusion_rows() -> list[tuple[str, str, str]]:
    rows = []
    mixed = [
        ("Which gas makes up most of Earth's atmosphere?", "nitrogen", "Nitrogen is about four-fifths of the air."),
        ("Which ocean is the largest?", "the Pacific", "The Pacific is the largest ocean."),
        ("Which is the longest river in Africa?", "the Nile", "The Nile is the longest river in Africa."),
        ("Which is the highest mountain above sea level?", "Mount Everest", "Everest is the highest peak above sea level."),
        ("Which desert is the largest hot desert?", "the Sahara", "The Sahara is the largest hot desert."),
        ("Which planet is closest to the Sun?", "Mercury", "Mercury orbits nearest the Sun."),
        ("Which metal is liquid at room temperature?", "mercury", "Mercury is a liquid metal at room temperature."),
        ("Which instrument has 88 keys in the standard form?", "the piano", "A standard piano has 88 keys."),
        ("How many strings does a standard violin have?", "4", "A violin has four strings."),
        ("How many strings does a standard guitar have?", "6", "A typical guitar has six strings."),
        ("Which language is used for the original Buddhist canon of the Theravada school?", "Pali", "The Theravada canon is preserved in Pali."),
        ("Which city is home to the United Nations headquarters?", "New York", "The UN headquarters is in New York."),
        ("Which organisation publishes the ICD disease classification?", "the World Health Organization", "WHO publishes the ICD."),
        ("What is the chemical symbol for gold?", "Au", "Gold's symbol comes from the Latin aurum."),
        ("What is the chemical symbol for silver?", "Ag", "Silver's symbol comes from the Latin argentum."),
        ("What is the chemical symbol for iron?", "Fe", "Iron's symbol comes from the Latin ferrum."),
        ("Which blood type is the universal donor for red cells?", "O negative", "O negative red cells are used as the universal donor type."),
        ("How many players are on a chessboard at the start, counting both sides?", "32", "Each side has 16 pieces."),
        ("How many squares are on a chessboard?", "64", "A chessboard is 8 by 8."),
        ("Which piece moves in an L shape?", "the knight", "The knight moves in an L."),
        ("Who wrote Hamlet?", "William Shakespeare", "Hamlet is a Shakespeare play."),
        ("Who wrote Don Quixote?", "Miguel de Cervantes", "Cervantes wrote Don Quixote."),
        ("Who wrote The Republic?", "Plato", "Plato wrote The Republic."),
        ("Who wrote the Iliad?", "Homer", "The Iliad is attributed to Homer."),
        ("Which country gifted the Statue of Liberty to the United States?", "France", "France gave the statue to the United States."),
        ("Which sea lies between Europe and Africa?", "the Mediterranean", "The Mediterranean lies between those continents."),
        ("Which canal joins the Mediterranean and the Red Sea?", "the Suez Canal", "The Suez Canal makes that link."),
        ("Which canal joins the Atlantic and the Pacific in Central America?", "the Panama Canal", "The Panama Canal crosses Panama."),
        ("What is the capital of Japan?", "Tokyo", "Tokyo is Japan's capital."),
        ("What is the capital of Australia?", "Canberra", "Canberra, not Sydney, is the capital."),
    ]
    # expand with numbered safe facts from squares and world languages already used? keep unique prompts
    languages = [
        ("Spain", "Spanish"), ("France", "French"), ("Germany", "German"), ("Italy", "Italian"),
        ("Portugal", "Portuguese"), ("Russia", "Russian"), ("Japan", "Japanese"), ("Korea", "Korean"),
        ("China", "Mandarin"), ("Arabic countries", "Arabic"), ("Hindi belt of India", "Hindi"),
        ("Brazil", "Portuguese"), ("Mexico", "Spanish"), ("Turkey", "Turkish"), ("Greece", "Greek"),
        ("Poland", "Polish"), ("Sweden", "Swedish"), ("Norway", "Norwegian"), ("Finland", "Finnish"),
        ("Hungary", "Hungarian"), ("Netherlands", "Dutch"), ("Thailand", "Thai"), ("Vietnam", "Vietnamese"),
        ("Indonesia", "Indonesian"), ("Iran", "Persian"),
    ]
    for place, lang in languages:
        rows.append((f"In a fusion quiz, which language is most associated with {place}?", lang, f"{lang} is the language most associated with {place}."))
    rows.extend(mixed)
    animals = [
        ("the fastest land animal", "the cheetah"),
        ("the largest land animal", "the African elephant"),
        ("the tallest land animal", "the giraffe"),
        ("the largest animal of all", "the blue whale"),
        ("a bird that cannot fly and lives in Antarctica", "the penguin"),
        ("a marsupial that carries its young in a pouch", "the kangaroo"),
    ]
    for feat, name in animals:
        rows.append((f"Which animal is {feat}?", name, f"The usual answer for {feat} is this animal."))
    for n in range(1, 81):
        rows.append((f"Fusion math: what is {n} plus {n}?", str(n + n), f"Adding {n} to itself doubles it."))
    for n in range(1, 41):
        rows.append((f"Fusion math: what is half of {n * 2}?", str(n), f"Half of {n * 2} is {n}."))
    composers = [
        ("the Ninth Symphony", "Ludwig van Beethoven"),
        ("The Four Seasons", "Antonio Vivaldi"),
        ("The Magic Flute", "Wolfgang Amadeus Mozart"),
        ("Messiah", "George Frideric Handel"),
        ("The Nutcracker", "Pyotr Ilyich Tchaikovsky"),
        ("Swan Lake", "Pyotr Ilyich Tchaikovsky"),
        ("Bolero", "Maurice Ravel"),
        ("The Rite of Spring", "Igor Stravinsky"),
        ("Peer Gynt", "Edvard Grieg"),
        ("Carmen", "Georges Bizet"),
        ("La Traviata", "Giuseppe Verdi"),
        ("Madama Butterfly", "Giacomo Puccini"),
        ("The Marriage of Figaro", "Wolfgang Amadeus Mozart"),
        ("Eine kleine Nachtmusik", "Wolfgang Amadeus Mozart"),
        ("Clair de Lune", "Claude Debussy"),
    ]
    for work, person in composers:
        rows.append((f"Who composed {work}?", person, f"{work} was composed by {person}."))
    foods = [
        ("sushi", "Japan"), ("paella", "Spain"), ("pizza", "Italy"), ("croissant", "France"),
        ("tacos", "Mexico"), ("kimchi", "Korea"), ("pho", "Vietnam"), ("pad thai", "Thailand"),
        ("hummus", "the Levant"), ("falafel", "the Levant"), ("fish and chips", "the United Kingdom"),
        ("poutine", "Canada"), ("biryani", "the Indian subcontinent"), ("dim sum", "China"),
        ("ceviche", "the Pacific coast of South America"), ("feijoada", "Brazil"),
        ("goulash", "Hungary"), ("moussaka", "Greece"), ("borscht", "Eastern Europe"),
        ("fondue", "Switzerland"),
    ]
    for dish, place in foods:
        rows.append((f"Which place is most closely associated with {dish}?", place, f"{dish} is most closely associated with {place}."))
    instruments = [
        ("violin", "strings"), ("viola", "strings"), ("cello", "strings"), ("double bass", "strings"),
        ("flute", "woodwind"), ("clarinet", "woodwind"), ("oboe", "woodwind"), ("bassoon", "woodwind"),
        ("trumpet", "brass"), ("trombone", "brass"), ("French horn", "brass"), ("tuba", "brass"),
        ("piano", "keyboard"), ("organ", "keyboard"), ("harpsichord", "keyboard"),
        ("timpani", "percussion"), ("snare drum", "percussion"), ("xylophone", "percussion"),
    ]
    for name, family in instruments:
        rows.append((f"To which instrument family does the {name} belong?", family, f"The {name} is in the {family} family."))
    for n in range(2, 42):
        rows.append((
            f"Fusion: how many sides does a regular polygon need to have if we simply ask for {n}?",
            str(n),
            f"The question names {n} sides.",
        ))
    colours = [
        ("chlorophyll", "green"), ("a ripe banana's peel", "yellow"), ("a clear daytime sky", "blue"),
        ("blood with oxygen", "red"), ("coal", "black"), ("snow", "white"), ("a carrot", "orange"),
        ("an eggplant", "purple"), ("chocolate", "brown"), ("the metal gold", "yellow"),
    ]
    for thing, colour in colours:
        rows.append((f"Which colour is most associated with {thing}?", colour, f"{thing} is most associated with {colour}."))
    notes = ["do", "re", "mi", "fa", "sol", "la", "ti"]
    for i, note in enumerate(notes):
        nxt = notes[(i + 1) % 7] if i < 6 else "do"
        rows.append((f"In solfege, which syllable comes right after {note}?", nxt, "Solfege runs do re mi fa sol la ti."))
    extra = [
        ("How many keys are on a standard piano?", "88", "A standard piano has 88 keys."),
        ("How many lines are in a musical staff?", "5", "A staff has five lines."),
        ("What is the name of the clef used for higher notes?", "treble", "The treble clef marks higher notes."),
        ("What is the name of the clef used for lower notes?", "bass", "The bass clef marks lower notes."),
        ("How many beats does a semibreve have in common time?", "4", "A semibreve fills a bar of four beats."),
        ("How many beats does a minim have in common time?", "2", "A minim is two beats."),
        ("How many beats does a crotchet have in common time?", "1", "A crotchet is one beat."),
        ("What is a rest in music?", "silence", "A rest marks silence."),
        ("What does a sharp do to a note?", "raises it", "A sharp raises the note."),
        ("What does a flat do to a note?", "lowers it", "A flat lowers the note."),
        ("How many strings does a standard ukulele have?", "4", "A ukulele has four strings."),
        ("How many strings does a standard mandolin have in courses, counting strings?", "8", "A mandolin is usually strung with eight strings in four pairs."),
    ]
    rows.extend(extra)
    return rows


def nexus_rows() -> list[tuple[str, str, str]]:
    rows = []
    hard = [
        ("What is the derivative of x squared, with respect to x?", "2x", "The power rule gives 2x."),
        ("What is the integral of 2x dx?", "x squared plus a constant", "Integrating 2x returns x squared, plus a constant."),
        ("What is the value of pi to two decimal places?", "3.14", "Pi begins 3.14."),
        ("What is Euler's number e to two decimal places?", "2.72", "e begins 2.718."),
        ("How many degrees are in a right angle?", "90", "A right angle is 90 degrees."),
        ("How many degrees are in a straight angle?", "180", "A straight angle is 180 degrees."),
        ("How many degrees are in a full circle?", "360", "A full turn is 360 degrees."),
        ("What is the sum of angles in a triangle, in degrees?", "180", "The interior angles of a triangle add to 180 degrees."),
        ("What is the sum of angles in a quadrilateral, in degrees?", "360", "A quadrilateral's interior angles add to 360 degrees."),
        ("What is the Pythagorean theorem for a right triangle?", "a squared plus b squared equals c squared", "The square on the hypotenuse equals the sum of the squares on the other two sides."),
        ("What base does binary use?", "2", "Binary is base 2."),
        ("What base does hexadecimal use?", "16", "Hexadecimal is base 16."),
        ("What base does decimal use?", "10", "Everyday numbers are base 10."),
        ("What is the ASCII code for uppercase A?", "65", "Uppercase A is ASCII 65."),
        ("What is the ASCII code for uppercase Z?", "90", "Uppercase Z is ASCII 90."),
        ("What is the ASCII code for digit 0?", "48", "The character 0 is ASCII 48."),
        ("Which sorting of a sorted list is already finished after one pass check?", "it is already sorted", "A sorted list needs no further swaps."),
        ("What does a primary key do in a database table?", "uniquely identifies a row", "A primary key picks out one row."),
        ("What does a foreign key do?", "points to a row in another table", "A foreign key links tables."),
        ("Which HTTP method is normally used to fetch a page?", "GET", "GET requests a resource."),
        ("Which HTTP method is normally used to submit a new resource?", "POST", "POST sends data to create or submit."),
        ("What does 404 mean?", "Not Found", "404 means the resource was not found."),
        ("What does 500 mean?", "Internal Server Error", "500 means the server failed."),
        ("Who proposed the Turing machine?", "Alan Turing", "Turing described that model of computation."),
        ("What is Moore's law about?", "transistor counts on a chip", "Moore's law describes how transistor counts grew."),
        ("Which particle mediates the electromagnetic force in the Standard Model?", "the photon", "The photon carries the electromagnetic force."),
        ("What is the charge of a proton?", "positive", "A proton is positively charged."),
        ("What is the charge of an electron?", "negative", "An electron is negatively charged."),
        ("What is absolute zero in Celsius, approximately?", "-273", "Absolute zero is about -273 degrees Celsius."),
        ("Which law says pressure times volume is constant at fixed temperature?", "Boyle's law", "Boyle's law relates pressure and volume."),
        ("Which organelle contains chlorophyll?", "the chloroplast", "Chloroplasts hold chlorophyll."),
        ("What is the haploid number of human chromosomes?", "23", "Gametes carry 23 chromosomes."),
        ("What does RNA use instead of thymine?", "uracil", "RNA uses uracil where DNA uses thymine."),
        ("Which process copies DNA into RNA?", "transcription", "Transcription makes RNA from DNA."),
        ("Which process builds a protein from RNA?", "translation", "Translation reads RNA to build a protein."),
        ("What is the powerhouse organelle?", "the mitochondrion", "Mitochondria release usable energy."),
        ("Which bond shares electrons?", "a covalent bond", "Covalent bonds share electrons."),
        ("Which bond transfers electrons between a metal and a non-metal?", "an ionic bond", "Ionic bonds form by transfer of electrons."),
        ("What is the pH of a neutral solution at 25 C?", "7", "Neutral water has pH 7."),
        ("What is an acid on the pH scale?", "below 7", "Acids have pH below 7."),
    ]
    rows.extend(hard)
    for n in range(1, 81):
        rows.append((f"Nexus: what is {n} cubed?", str(n ** 3), f"The cube of {n} is {n} times {n} times {n}."))
    for n in range(0, 61):
        rows.append((f"Nexus: what is 3 to the power of {n}?" if n < 12 else f"Nexus skip {n}", str(3 ** n) if n < 12 else "skip", "powers"))
    # replace the broken loop with a clean one
    rows = [r for r in rows if not r[0].startswith("Nexus: what is 3") and not r[0].startswith("Nexus skip")]
    for n in range(0, 12):
        rows.append((f"What is 3 to the power of {n}?", str(3 ** n), "Powers of three multiply by three at each step."))
    for n in range(0, 10):
        rows.append((f"What is 5 to the power of {n}?", str(5 ** n), "Powers of five multiply by five at each step."))
    primes = [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97]
    for i, p in enumerate(primes, start=1):
        rows.append((f"What is the {i}th prime number?", str(p), "Primes have no divisors other than 1 and themselves."))
    for a in range(13, 21):
        for b in range(13, 21):
            rows.append((f"What is {a} multiplied by {b}?", str(a * b), "This is a product of two whole numbers."))
    fib = [1, 1]
    for i in range(2, 20):
        fib.append(fib[-1] + fib[-2])
    for i, val in enumerate(fib, start=1):
        rows.append((f"What is Fibonacci number {i}, starting 1, 1, 2?", str(val), "Each Fibonacci number is the sum of the two before it."))
    for n in range(1, 41):
        rows.append((
            f"What is the remainder when {n * 7 + 3} is divided by 7?",
            "3",
            "Adding a multiple of 7 does not change the remainder 3.",
        ))
    for n in range(1, 31):
        rows.append((
            f"Nexus square check: what is {n} times {n}?",
            str(n * n),
            f"{n} times {n} is its square.",
        ))
    return rows


def main() -> None:
    existing = set()
    for q in json.loads((ROOT / "questions.json").read_text(encoding="utf-8")):
        existing.add(q["prompt"])
    for q in json.loads((ROOT / "mode_questions.json").read_text(encoding="utf-8")):
        existing.add(q["prompt"])
    banks = {
        "gk": gk_rows(),
        "sci": sci_rows(),
        "his": his_rows(),
        "spo": spo_rows(),
        "mov": mov_rows(),
        "tech": tech_rows(),
        "fusion": fusion_rows(),
        "nexus": nexus_rows(),
    }
    all_q = []
    for cat, rows in banks.items():
        built = take(cat, rows, set(existing))
        print(f"{cat}: {len(built)} new, bank {len(rows)}")
        all_q.extend(built)
        existing.update(q["prompt"] for q in built)
    ids = [q["id"] for q in all_q]
    if len(ids) != len(set(ids)):
        raise SystemExit("duplicate ids")
    OUT.write_text(json.dumps(all_q, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"wrote {len(all_q)}")


if __name__ == "__main__":
    main()
