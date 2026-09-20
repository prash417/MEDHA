"""Build 200 original 4-option questions per MEDHA category and write tools/questions.json."""
from __future__ import annotations

import json
import random
from pathlib import Path

OUT = Path(__file__).resolve().parent / "questions.json"

CATEGORIES = [
    ("gk", "General Knowledge"),
    ("sci", "Science"),
    ("his", "History"),
    ("spo", "Sports"),
    ("mov", "Movies"),
    ("tech", "Technology"),
    ("fusion", "Fusion"),
    ("nexus", "Nexus"),
]


def diff_for(n: int) -> str:
    if n <= 50:
        return "EASY"
    if n <= 110:
        return "MEDIUM"
    if n <= 160:
        return "HARD"
    return "EXPERT"


def pack(prompt: str, correct: str, wrongs: list[str], explanation: str) -> tuple[list[str], str]:
    opts = [correct] + wrongs[:3]
    rng = random.Random(prompt)
    rng.shuffle(opts)
    letter = "ABCD"[opts.index(correct)]
    return opts, letter


def item(cat: str, n: int, prompt: str, correct: str, wrongs: list[str], explanation: str) -> dict:
    opts, letter = pack(prompt, correct, wrongs, explanation)
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
        "tags": [cat],
        "source": "medha-seed",
    }


CAPITALS = [tuple(x.split("|")) for x in """
Afghanistan|Kabul|Asia
Albania|Tirana|Europe
Algeria|Algiers|Africa
Argentina|Buenos Aires|South America
Australia|Canberra|Oceania
Austria|Vienna|Europe
Bangladesh|Dhaka|Asia
Belgium|Brussels|Europe
Bhutan|Thimphu|Asia
Brazil|Brasília|South America
Bulgaria|Sofia|Europe
Cambodia|Phnom Penh|Asia
Canada|Ottawa|North America
Chile|Santiago|South America
China|Beijing|Asia
Colombia|Bogotá|South America
Croatia|Zagreb|Europe
Cuba|Havana|North America
Czechia|Prague|Europe
Denmark|Copenhagen|Europe
Egypt|Cairo|Africa
Ethiopia|Addis Ababa|Africa
Finland|Helsinki|Europe
France|Paris|Europe
Germany|Berlin|Europe
Ghana|Accra|Africa
Greece|Athens|Europe
Hungary|Budapest|Europe
Iceland|Reykjavík|Europe
India|New Delhi|Asia
Indonesia|Jakarta|Asia
Iran|Tehran|Asia
Iraq|Baghdad|Asia
Ireland|Dublin|Europe
Israel|Jerusalem|Asia
Italy|Rome|Europe
Japan|Tokyo|Asia
Jordan|Amman|Asia
Kenya|Nairobi|Africa
Kuwait|Kuwait City|Asia
Malaysia|Kuala Lumpur|Asia
Maldives|Malé|Asia
Mexico|Mexico City|North America
Mongolia|Ulaanbaatar|Asia
Morocco|Rabat|Africa
Myanmar|Naypyidaw|Asia
Nepal|Kathmandu|Asia
Netherlands|Amsterdam|Europe
New Zealand|Wellington|Oceania
Nigeria|Abuja|Africa
Norway|Oslo|Europe
Oman|Muscat|Asia
Pakistan|Islamabad|Asia
Peru|Lima|South America
Philippines|Manila|Asia
Poland|Warsaw|Europe
Portugal|Lisbon|Europe
Qatar|Doha|Asia
Romania|Bucharest|Europe
Russia|Moscow|Europe
Saudi Arabia|Riyadh|Asia
Serbia|Belgrade|Europe
Singapore|Singapore|Asia
Slovakia|Bratislava|Europe
South Africa|Pretoria|Africa
South Korea|Seoul|Asia
Spain|Madrid|Europe
Sri Lanka|Sri Jayawardenepura Kotte|Asia
Sweden|Stockholm|Europe
Switzerland|Bern|Europe
Thailand|Bangkok|Asia
Tunisia|Tunis|Africa
Turkey|Ankara|Asia
UAE|Abu Dhabi|Asia
United Kingdom|London|Europe
Ukraine|Kyiv|Europe
United States|Washington, D.C.|North America
Vietnam|Hanoi|Asia
Zambia|Lusaka|Africa
Zimbabwe|Harare|Africa
Armenia|Yerevan|Asia
Azerbaijan|Baku|Asia
Belarus|Minsk|Europe
Estonia|Tallinn|Europe
Georgia|Tbilisi|Asia
Kazakhstan|Astana|Asia
Latvia|Riga|Europe
Lithuania|Vilnius|Europe
Malta|Valletta|Europe
Panama|Panama City|North America
Paraguay|Asunción|South America
Uruguay|Montevideo|South America
Uzbekistan|Tashkent|Asia
Venezuela|Caracas|South America
Bolivia|Sucre|South America
Cyprus|Nicosia|Asia
Luxembourg|Luxembourg|Europe
Tanzania|Dodoma|Africa
Uganda|Kampala|Africa
Senegal|Dakar|Africa
""".strip().splitlines()]

ELEMENTS = [tuple(x.split("|")) for x in """
Hydrogen|H|1
Helium|He|2
Lithium|Li|3
Beryllium|Be|4
Boron|B|5
Carbon|C|6
Nitrogen|N|7
Oxygen|O|8
Fluorine|F|9
Neon|Ne|10
Sodium|Na|11
Magnesium|Mg|12
Aluminium|Al|13
Silicon|Si|14
Phosphorus|P|15
Sulfur|S|16
Chlorine|Cl|17
Argon|Ar|18
Potassium|K|19
Calcium|Ca|20
Scandium|Sc|21
Titanium|Ti|22
Vanadium|V|23
Chromium|Cr|24
Manganese|Mn|25
Iron|Fe|26
Cobalt|Co|27
Nickel|Ni|28
Copper|Cu|29
Zinc|Zn|30
Gallium|Ga|31
Germanium|Ge|32
Arsenic|As|33
Selenium|Se|34
Bromine|Br|35
Krypton|Kr|36
Rubidium|Rb|37
Strontium|Sr|38
Yttrium|Y|39
Zirconium|Zr|40
Niobium|Nb|41
Molybdenum|Mo|42
Technetium|Tc|43
Ruthenium|Ru|44
Rhodium|Rh|45
Palladium|Pd|46
Silver|Ag|47
Cadmium|Cd|48
Indium|In|49
Tin|Sn|50
Antimony|Sb|51
Tellurium|Te|52
Iodine|I|53
Xenon|Xe|54
Caesium|Cs|55
Barium|Ba|56
Lanthanum|La|57
Cerium|Ce|58
Praseodymium|Pr|59
Neodymium|Nd|60
Promethium|Pm|61
Samarium|Sm|62
Europium|Eu|63
Gadolinium|Gd|64
Terbium|Tb|65
Dysprosium|Dy|66
Holmium|Ho|67
Erbium|Er|68
Thulium|Tm|69
Ytterbium|Yb|70
Lutetium|Lu|71
Hafnium|Hf|72
Tantalum|Ta|73
Tungsten|W|74
Rhenium|Re|75
Osmium|Os|76
Iridium|Ir|77
Platinum|Pt|78
Gold|Au|79
Mercury|Hg|80
Thallium|Tl|81
Lead|Pb|82
Bismuth|Bi|83
Polonium|Po|84
Astatine|At|85
Radon|Rn|86
Francium|Fr|87
Radium|Ra|88
Actinium|Ac|89
Thorium|Th|90
Protactinium|Pa|91
Uranium|U|92
Neptunium|Np|93
Plutonium|Pu|94
Americium|Am|95
Curium|Cm|96
Berkelium|Bk|97
Californium|Cf|98
Einsteinium|Es|99
Fermium|Fm|100
""".strip().splitlines()]

HISTORY = [tuple(x.split("|")) for x in """
1947|India became independent from British rule|Independence of India
1857|The first major uprising against the East India Company began|Indian Rebellion of 1857
1919|British troops fired on a crowd at Jallianwala Bagh|Jallianwala Bagh massacre
1930|Gandhi marched from Sabarmati to Dandi against the salt tax|Salt March
1942|The Quit India Movement was launched|Quit India Movement
1950|The Constitution of India came into force|Republic Day origin
1526|The First Battle of Panipat brought Mughal power to India|First Battle of Panipat
1757|The Battle of Plassey opened Company dominance in Bengal|Battle of Plassey
1858|The British Crown took over from the East India Company|Government of India Act 1858
1915|Gandhi returned to India from South Africa|Gandhi's return
1920|The Non-Cooperation Movement was launched|Non-Cooperation Movement
1948|Mahatma Gandhi was assassinated in New Delhi|Assassination of Gandhi
1961|Indian forces ended Portuguese rule in Goa|Liberation of Goa
1962|India fought a border war with China|Sino-Indian War
1965|India and Pakistan fought a major war over Kashmir|Indo-Pakistani War of 1965
1971|Bangladesh emerged as an independent nation after war|Bangladesh Liberation War
1974|India conducted its first nuclear test at Pokhran|Smiling Buddha
1991|India began major economic liberalisation|1991 reforms
1998|India conducted the Pokhran-II nuclear tests|Pokhran-II
2008|India signed the civil nuclear agreement with the United States|India–US civil nuclear deal
326|Alexander the Great fought King Porus near the Hydaspes|Battle of the Hydaspes
261|Ashoka is said to have turned toward dhamma after the Kalinga war|Kalinga War
1192|Prithviraj Chauhan was defeated at the Second Battle of Tarain|Second Battle of Tarain
1336|The Vijayanagara Empire was founded|Founding of Vijayanagara
1498|Vasco da Gama reached Calicut|Da Gama in India
1600|The English East India Company received its first charter|EIC charter
1761|The Marathas were defeated at the Third Battle of Panipat|Third Battle of Panipat
1773|The Regulating Act first tried to control Company rule|Regulating Act
1784|Pitt's India Act tightened British parliamentary control|Pitt's India Act
1818|The Third Anglo-Maratha War ended Maratha confederacy power|Third Anglo-Maratha War
1843|Sindh was annexed by the British|Annexation of Sindh
1849|Punjab was annexed after the Anglo-Sikh wars|Annexation of Punjab
1856|Awadh was annexed under the Doctrine of Lapse policy era|Annexation of Awadh
1869|Mahatma Gandhi was born in Porbandar|Birth of Gandhi
1876|Queen Victoria was proclaimed Empress of India|Empress of India
1885|The Indian National Congress was founded|Founding of INC
1905|The Partition of Bengal was announced by Curzon|Partition of Bengal
1906|The All-India Muslim League was founded|Muslim League founded
1911|The capital of British India moved from Calcutta to Delhi|Capital shift to Delhi
1916|Congress and the League signed the Lucknow Pact|Lucknow Pact
1922|Gandhi withdrew Non-Cooperation after Chauri Chaura|Chauri Chaura
1928|The Simon Commission arrived in India and was boycotted|Simon Commission
1929|Congress declared Purna Swaraj at Lahore|Purna Swaraj
1931|The Gandhi–Irwin Pact was signed|Gandhi–Irwin Pact
1935|The Government of India Act expanded provincial autonomy|GOI Act 1935
1940|The Lahore Resolution was adopted|Lahore Resolution
1946|The Royal Indian Navy mutiny broke out|RIN mutiny
1946|The Cabinet Mission came to India|Cabinet Mission
1947|India was partitioned, creating Pakistan|Partition of India
1952|India held its first general election|First general election
1956|States were reorganised largely on linguistic lines|States Reorganisation
1966|Indira Gandhi first became Prime Minister of India|Indira Gandhi PM
1975|The Emergency was declared in India|The Emergency
1984|Operation Blue Star was carried out in Amritsar|Operation Blue Star
1992|The Babri Masjid in Ayodhya was demolished|Babri Masjid demolition
1999|India and Pakistan fought the Kargil conflict|Kargil War
2000|Three new Indian states were created: Chhattisgarh, Uttarakhand, Jharkhand|New states 2000
2014|Narendra Modi first became Prime Minister of India|Modi first term
2019|Article 370's special status for J&K was revoked|Article 370
1776|The United States Declaration of Independence was adopted|US Independence
1789|The French Revolution began with the storming of the Bastille|French Revolution
1815|Napoleon was defeated at Waterloo|Battle of Waterloo
1865|The American Civil War ended|End of US Civil War
1914|The First World War began|Start of WWI
1917|The October Revolution took place in Russia|Russian Revolution
1918|The First World War armistice was signed|End of WWI
1939|The Second World War began in Europe|Start of WWII
1945|The United Nations was founded|Founding of UN
1945|The United States dropped atomic bombs on Hiroshima and Nagasaki|Atomic bombs 1945
1949|The People's Republic of China was proclaimed|PRC founded
1961|Yuri Gagarin became the first human in space|Gagarin flight
1963|Martin Luther King Jr delivered the I Have a Dream speech|March on Washington
1969|Apollo 11 landed humans on the Moon|Moon landing
1989|The Berlin Wall fell|Fall of Berlin Wall
1991|The Soviet Union was dissolved|USSR collapse
2001|The World Trade Center was attacked on 11 September|September 11
""".strip().splitlines()]

SPORTS = [tuple(x.split("|")) for x in """
Cricket|India won the 1983 Men's Cricket World Cup|Kapil Dev
Cricket|India won the 2011 Men's Cricket World Cup|MS Dhoni
Cricket|India won the inaugural 2007 Men's T20 World Cup|MS Dhoni
Cricket|Australia has won the most Men's Cricket World Cups|Australia
Cricket|Sachin Tendulkar scored 100 international centuries|Sachin Tendulkar
Cricket|The Ashes is contested by England and Australia|The Ashes
Cricket|Eden Gardens is a historic cricket ground in Kolkata|Eden Gardens
Cricket|Lord's is widely called the home of cricket|Lord's
Cricket|The IPL began in 2008|IPL
Cricket|Virender Sehwag was known for explosive Test opening|Virender Sehwag
Cricket|Anil Kumble took 10 wickets in a Test innings vs Pakistan|Anil Kumble
Cricket|Jim Laker took 19 wickets in a Test match|Jim Laker
Cricket|Don Bradman retired with a Test average of 99.94|Don Bradman
Cricket|The first Men's Cricket World Cup was in 1975|1975
Cricket|West Indies won the first two Men's Cricket World Cups|West Indies
Cricket|Muttiah Muralitharan is among Test cricket's leading wicket-takers|Muralitharan
Cricket|Brian Lara scored 400 not out in a Test innings|Brian Lara
Cricket|The Ranji Trophy is India's premier domestic first-class competition|Ranji Trophy
Cricket|The Irani Cup is an Indian first-class cricket fixture|Irani Cup
Cricket|Wankhede Stadium hosted the 2011 World Cup final|Wankhede Stadium
Football|Brazil has won the most FIFA Men's World Cups|Brazil
Football|The FIFA World Cup is held every four years|Four years
Football|Uruguay won the first FIFA World Cup in 1930|Uruguay
Football|Argentina won the 2022 FIFA World Cup|Argentina
Football|Lionel Messi won the 2022 World Cup with Argentina|Lionel Messi
Football|Cristiano Ronaldo has starred for Portugal at World Cups|Cristiano Ronaldo
Football|The UEFA Champions League is Europe's top club competition|Champions League
Football|FIFA headquarters are in Zurich|Zurich
Football|The offside rule is a core football law|Offside
Football|Pelé won three World Cups with Brazil|Pelé
Football|Diego Maradona captained Argentina to the 1986 World Cup|Maradona
Football|The Premier League is England's top football division|Premier League
Football|La Liga is Spain's top football division|La Liga
Football|The FIFA Women's World Cup began in 1991|1991
Football|The Indian Super League is a top Indian football league|ISL
Football|Sunil Chhetri is India's leading men's football goal-scorer|Sunil Chhetri
Football|The 2010 World Cup was hosted by South Africa|South Africa
Football|The 2014 World Cup was hosted by Brazil|Brazil
Football|The 2018 World Cup was hosted by Russia|Russia
Football|The 2002 World Cup was co-hosted by South Korea and Japan|Korea/Japan
Olympics|The modern Olympic Games began in Athens in 1896|Athens 1896
Olympics|The Olympic rings represent five inhabited continents|Five rings
Olympics|Pierre de Coubertin helped revive the modern Olympics|Coubertin
Olympics|The 2008 Summer Olympics were hosted by Beijing|Beijing 2008
Olympics|The 2012 Summer Olympics were hosted by London|London 2012
Olympics|The 2016 Summer Olympics were hosted by Rio de Janeiro|Rio 2016
Olympics|The 2020 Summer Olympics were held in Tokyo in 2021|Tokyo 2020
Olympics|Abhinav Bindra won India's first individual Olympic gold|Abhinav Bindra
Olympics|Neeraj Chopra won Olympic gold in javelin at Tokyo 2020|Neeraj Chopra
Olympics|PV Sindhu won Olympic medals in badminton|PV Sindhu
Olympics|Mary Kom is an Olympic boxing medallist from India|Mary Kom
Olympics|Usain Bolt won Olympic gold in the 100m and 200m|Usain Bolt
Olympics|Michael Phelps is the most decorated Olympian|Michael Phelps
Olympics|The Winter Olympics include skiing and skating events|Winter Games
Olympics|The marathon commemorates a run linked to ancient Greece|Marathon
Olympics|The Olympic motto is Citius, Altius, Fortius|Faster Higher Stronger
Olympics|India's first Olympic hockey gold came in 1928|1928
Olympics|The Indian men's hockey team has multiple Olympic golds|Indian hockey
Olympics|The 1980 Moscow Olympics were boycotted by several nations|Moscow 1980
Olympics|The 1984 Los Angeles Olympics were boycotted by the Soviet bloc|LA 1984
Tennis|Wimbledon is played on grass courts|Grass
Tennis|The Australian Open is a tennis Grand Slam|Australian Open
Tennis|Roland Garros hosts the French Open on clay|French Open
Tennis|The US Open is played in New York|US Open
Tennis|A Grand Slam means winning all four majors in a year|Grand Slam
Tennis|Rafael Nadal is closely associated with clay-court dominance|Rafael Nadal
Tennis|Roger Federer won a record number of Wimbledon men's titles in his era|Roger Federer
Tennis|Serena Williams is among the most successful women's players|Serena Williams
Tennis|Leander Paes won Olympic bronze for India in 1996|Leander Paes
Tennis|Sania Mirza won multiple Grand Slam doubles titles|Sania Mirza
Tennis|Davis Cup is an international men's team tennis event|Davis Cup
Tennis|A tennis set is typically won by the first to 6 games with a margin|Set
Hockey|India's men's hockey team was coached in legend by Dhyan Chand's era|Dhyan Chand
Hockey|The Hockey World Cup is organised by the FIH|FIH
Hockey|A hockey field is 91.4 metres long|Hockey pitch
Badminton|The Thomas Cup is a men's team badminton event|Thomas Cup
Badminton|The Uber Cup is a women's team badminton event|Uber Cup
Badminton|Saina Nehwal won Olympic bronze in badminton|Saina Nehwal
Badminton|Prakash Padukone was a pioneering Indian badminton champion|Prakash Padukone
Chess|Viswanathan Anand is India's first World Chess Champion in the modern era|Viswanathan Anand
Chess|A chessboard has 64 squares|64
Chess|Checkmate ends a game of chess|Checkmate
Chess|The pawn is the most numerous chess piece|Pawn
F1|The Formula One World Championship began in 1950|1950
F1|Monaco hosts a famous street-circuit Grand Prix|Monaco
F1|Lewis Hamilton is among F1's most successful drivers|Lewis Hamilton
F1|Michael Schumacher won seven F1 world titles|Schumacher
Athletics|A standard outdoor running track is 400 metres per lap|400m
Athletics|The decathlon comprises ten events|Ten
Athletics|The 100 metres is a sprint event|100m
Kabaddi|Pro Kabaddi League is a major Indian kabaddi competition|PKL
Kabaddi|Kabaddi is a traditional contact sport popular in India|Kabaddi
Wrestling|Sushil Kumar is an Olympic wrestling medallist from India|Sushil Kumar
Wrestling|Bajrang Punia is an Olympic wrestling medallist from India|Bajrang Punia
Shooting|Rajyavardhan Singh Rathore won Olympic silver in shooting|Rathore
Shooting|Gagan Narang won Olympic bronze in shooting|Gagan Narang
Table tennis|Table tennis is also called ping-pong|Ping-pong
Golf|The Masters is played at Augusta National|Augusta
Golf|A golf course typically has 18 holes|18
Boxing|Vijender Singh won Olympic bronze in boxing|Vijender Singh
Swimming|An Olympic swimming pool is 50 metres long|50m
Cycling|The Tour de France is the most famous cycling stage race|Tour de France
Rugby|A rugby union team has 15 players|15
Basketball|A basketball team has five players on court|Five
Volleyball|A volleyball team has six players on court|Six
""".strip().splitlines()]

FILMS = [tuple(x.split("|")) for x in """
Sholay|1975|Ramesh Sippy
Mother India|1957|Mehboob Khan
Mughal-e-Azam|1960|K. Asif
Pather Panchali|1955|Satyajit Ray
Guide|1965|Vijay Anand
Deewaar|1975|Yash Chopra
Amar Akbar Anthony|1977|Manmohan Desai
Lagaan|2001|Ashutosh Gowariker
Dilwale Dulhania Le Jayenge|1995|Aditya Chopra
3 Idiots|2009|Rajkumar Hirani
Taare Zameen Par|2007|Aamir Khan
Rang De Basanti|2006|Rakeysh Omprakash Mehra
Black|2005|Sanjay Leela Bhansali
Devdas|2002|Sanjay Leela Bhansali
Hum Aapke Hain Koun..!|1994|Sooraj Barjatya
Kuch Kuch Hota Hai|1998|Karan Johar
Kabhi Khushi Kabhie Gham|2001|Karan Johar
Swades|2004|Ashutosh Gowariker
Chak De! India|2007|Shimit Amin
Gully Boy|2019|Zoya Akhtar
Andhadhun|2018|Sriram Raghavan
Article 15|2019|Anubhav Sinha
Dangal|2016|Nitesh Tiwari
Bajrangi Bhaijaan|2015|Kabir Khan
PK|2014|Rajkumar Hirani
Barfi!|2012|Anurag Basu
Udaan|2010|Vikramaditya Motwane
Haider|2014|Vishal Bhardwaj
Piku|2015|Shoojit Sircar
The Lunchbox|2013|Ritesh Batra
Court|2014|Chaitanya Tamhane
Masaan|2015|Neeraj Ghaywan
Newton|2017|Amit V Masurkar
Uri: The Surgical Strike|2019|Aditya Dhar
Drishyam|2015|Nishikant Kamat
Kahaani|2012|Sujoy Ghosh
Queen|2013|Vikas Bahl
Mary Kom|2014|Omung Kumar
Bhaag Milkha Bhaag|2013|Rakeysh Omprakash Mehra
Raazi|2018|Meghna Gulzar
Tumbbad|2018|Rahi Anil Barve
Ship of Theseus|2012|Anand Gandhi
Anand|1971|Hrishikesh Mukherjee
Gol Maal|1979|Hrishikesh Mukherjee
Chupke Chupke|1975|Hrishikesh Mukherjee
Pyaasa|1957|Guru Dutt
Kaagaz Ke Phool|1959|Guru Dutt
Do Bigha Zamin|1953|Bimal Roy
Bandit Queen|1994|Shekhar Kapur
Salaam Bombay!|1988|Mira Nair
Monsoon Wedding|2001|Mira Nair
The Apu Trilogy|1955|Satyajit Ray
Charulata|1964|Satyajit Ray
Nayakan|1987|Mani Ratnam
Roja|1992|Mani Ratnam
Bombay|1995|Mani Ratnam
Dil Se..|1998|Mani Ratnam
Alaipayuthey|2000|Mani Ratnam
Kannathil Muthamittal|2002|Mani Ratnam
Baahubali: The Beginning|2015|S. S. Rajamouli
Baahubali 2: The Conclusion|2017|S. S. Rajamouli
RRR|2022|S. S. Rajamouli
Enthiran|2010|S. Shankar
Indian|1996|S. Shankar
Anniyan|2005|S. Shankar
Nayagan|1987|Mani Ratnam
Thalapathi|1991|Mani Ratnam
Kaakha Kaakha|2003|Gautham Vasudev Menon
Super Deluxe|2019|Thiagarajan Kumararaja
Jigarthanda|2014|Karthik Subbaraj
Vikram|2022|Lokesh Kanagaraj
Kaithi|2019|Lokesh Kanagaraj
KGF: Chapter 1|2018|Prashanth Neel
KGF: Chapter 2|2022|Prashanth Neel
Kantara|2022|Rishab Shetty
Lucia|2013|Pawan Kumar
Ugramm|2014|Prashanth Neel
Mungaru Male|2006|Yogaraj Bhat
Citizen Kane|1941|Orson Welles
Casablanca|1942|Michael Curtiz
The Godfather|1972|Francis Ford Coppola
Pulp Fiction|1994|Quentin Tarantino
Schindler's List|1993|Steven Spielberg
Jurassic Park|1993|Steven Spielberg
E.T. the Extra-Terrestrial|1982|Steven Spielberg
Inception|2010|Christopher Nolan
The Dark Knight|2008|Christopher Nolan
Interstellar|2014|Christopher Nolan
Parasite|2019|Bong Joon-ho
Amélie|2001|Jean-Pierre Jeunet
Life Is Beautiful|1997|Roberto Benigni
Spirited Away|2001|Hayao Miyazaki
Seven Samurai|1954|Akira Kurosawa
Rashomon|1950|Akira Kurosawa
Bicycle Thieves|1948|Vittorio De Sica
The Shawshank Redemption|1994|Frank Darabont
Titanic|1997|James Cameron
Avatar|2009|James Cameron
Gravity|2013|Alfonso Cuarón
Roma|2018|Alfonso Cuarón
""".strip().splitlines()]

TECH = [tuple(x.split("|")) for x in """
World Wide Web|Tim Berners-Lee|1991
Email|Ray Tomlinson|1971
Telephone|Alexander Graham Bell|1876
Light bulb (practical)|Thomas Edison|1879
Radio|Guglielmo Marconi|1895
Television|John Logie Baird|1926
Transistor|Bell Labs team|1947
Integrated circuit|Jack Kilby|1958
Microprocessor|Intel 4004|1971
Personal computer IBM PC|IBM|1981
Macintosh|Apple|1984
Windows 95|Microsoft|1995
Google|Larry Page and Sergey Brin|1998
Facebook|Mark Zuckerberg|2004
YouTube|Chad Hurley, Steve Chen, Jawed Karim|2005
Twitter|Jack Dorsey and others|2006
iPhone|Apple|2007
Android|Google|2008
WhatsApp|Brian Acton and Jan Koum|2009
Instagram|Kevin Systrom and Mike Krieger|2010
Linux|Linus Torvalds|1991
Python|Guido van Rossum|1991
Java|James Gosling|1995
C language|Dennis Ritchie|1972
Unix|Ken Thompson and Dennis Ritchie|1969
Git|Linus Torvalds|2005
Wikipedia|Jimmy Wales and Larry Sanger|2001
USB|USB-IF / Intel-led|1996
Wi-Fi|IEEE 802.11|1997
Bluetooth|Ericsson-led SIG|1998
GPS opened to civilians|US government|1980s–1990s
HTTP|Tim Berners-Lee|1991
HTML|Tim Berners-Lee|1991
CSS|Håkon Wium Lie|1996
JavaScript|Brendan Eich|1995
TCP/IP|Vint Cerf and Bob Kahn|1970s
ARPANET|DARPA|1969
Domain Name System|Paul Mockapetris|1983
IPv4 address bits|32|1981
IPv6 address bits|128|1998
SSD vs HDD|flash memory vs spinning disks|2000s
Cloud computing term rise|Amazon AWS|2006
Bitcoin white paper|Satoshi Nakamoto|2008
ChatGPT public launch|OpenAI|2022
Chatbot ELIZA|Joseph Weizenbaum|1966
Deep Blue vs Kasparov|IBM|1997
AlphaGo vs Lee Sedol|DeepMind|2016
Hubble Space Telescope|NASA/ESA|1990
International Space Station|NASA and partners|1998
Mars Orbiter Mission|ISRO|2013
Chandrayaan-1|ISRO|2008
Chandrayaan-3|ISRO|2023
Aditya-L1|ISRO|2023
Aryabhata satellite|ISRO|1975
INSAT programme|ISRO|1980s
Aadhaar|UIDAI|2009
UPI|NPCI|2016
IRCTC website|Indian Railways|2002
BHIM app|NPCI|2016
Jio 4G launch|Reliance Jio|2016
CDAC PARAM supercomputer|India|1991
Semiconductor silicon|integrated circuits|1950s
OLED display|organic LEDs|2000s
QR code|Denso Wave|1994
PDF|Adobe|1993
MP3 audio format|Fraunhofer|1990s
JPEG image format|JPEG committee|1992
PNG image format|PNG group|1996
ZIP compression|Phil Katz|1989
Bluetooth name origin|Harald Bluetooth|1990s
Ethernet|Robert Metcalfe|1973
Laser|Townes / Maiman era|1960
Optical fibre communications|Charles Kao|1960s
Barcode|N. Joseph Woodland|1952
ATM|John Shepherd-Barron era|1960s
Credit card magnetic stripe|IBM|1960s
Touchscreen capacitive rise|smartphones|2007
3D printing / stereolithography|Chuck Hull|1983
Drone consumer boom|DJI era|2010s
Electric car mass-market Tesla Roadster|Tesla|2008
Hypertext|Ted Nelson|1960s
Mouse|Douglas Engelbart|1964
GUI popularised|Xerox PARC / Apple|1970s–80s
Spreadsheet VisiCalc|Bricklin and Frankston|1979
Relational database|Edgar F. Codd|1970
SQL|IBM|1970s
NoSQL term rise|tech community|2009
Docker containers|Solomon Hykes|2013
Kubernetes|Google|2014
Raspberry Pi|Raspberry Pi Foundation|2012
Arduino|Ivrea / Banzi et al.|2005
IoT term popularised|Kevin Ashton|1999
5G mobile standard|3GPP|2010s
4G LTE|3GPP|2009
3G UMTS|3GPP|1999
2G GSM|ETSI|1991
Fibre to home India boom|Jio / Airtel|2010s
National Knowledge Network|India|2010
Digital India programme|Government of India|2015
MyGov platform|Government of India|2014
DigiLocker|MeitY|2015
CoWIN platform|Government of India|2021
ONDC|Government of India|2022
India Stack|Aadhaar ecosystem|2010s
""".strip().splitlines()]

FUSION = [tuple(x.split("|")) for x in """
Bengaluru is the headquarters of which Indian space agency?|ISRO|DRDO|BARC|HAL|ISRO's headquarters are in Bengaluru, Karnataka.
Which Indian physicist is honoured on National Science Day for the Raman effect?|C. V. Raman|Homi Bhabha|Meghnad Saha|S. N. Bose|National Science Day marks Raman's 1928 discovery of inelastic scattering of light.
The Mars Orbiter Mission was launched by which country?|India|Japan|France|Australia|ISRO's Mangalyaan reached Martian orbit in 2014.
Eden Gardens stands in which Indian city?|Kolkata|Mumbai|Chennai|Delhi|The historic cricket ground is in Kolkata, West Bengal.
Wankhede Stadium is in which city that is also Maharashtra's capital?|Mumbai|Pune|Nagpur|Nashik|Mumbai hosts Wankhede and is Maharashtra's capital.
Which film about a village cricket match was nominated for the Best Foreign Language Oscar?|Lagaan|Lagaan is a 2001 Ashutosh Gowariker film set around a colonial-era cricket wager.|Swades|Chak De! India|Iqbal
Which scientist from India won a Nobel Prize in Physics in 1930?|C. V. Raman|Rabindranath Tagore|Amartya Sen|Kailash Satyarthi|Raman received the 1930 physics prize for light scattering.
Tagore won the Nobel Prize in which field?|Literature|Physics|Peace|Economics|Gitanjali brought Tagore the 1913 literature prize.
The Chipko movement combined environmentalism with which Indian region?|Himalaya|Sundarbans|Thar|Western Ghats|Chipko grew in the Himalayan belt in the 1970s.
Jallianwala Bagh is in which city also holy in Sikh history?|Amritsar|Ludhiana|Patiala|Jalandhar|The 1919 massacre occurred in Amritsar.
The Salt March ended at which coastal village in Gujarat?|Dandi|Dwarka|Mandvi|Porbandar|Gandhi reached Dandi in 1930.
Porbandar, Gandhi's birthplace, lies in which Indian state?|Gujarat|Maharashtra|Rajasthan|Goa|Gandhi was born in Porbandar, Gujarat.
Chandrayaan-3 landed near the Moon's south pole for which country?|India|USA|China|Japan|ISRO's 2023 landing was the first near the lunar south pole.
Which Indian city is both a film hub nicknamed Bollywood and Maharashtra's capital?|Mumbai|Hyderabad|Chennai|Kolkata|Mumbai is the centre of Hindi cinema and the state capital.
Satyajit Ray's Pather Panchali is set in which Indian state?|West Bengal|Odisha|Bihar|Assam|The film follows a Bengali village family.
The 2011 Cricket World Cup final was played in which Indian city?|Mumbai|Delhi|Kolkata|Chennai|India beat Sri Lanka at Wankhede, Mumbai.
Neeraj Chopra's Olympic gold was in which athletics event?|Javelin throw|Shot put|Discus|Hammer throw|Chopra won javelin gold at Tokyo 2020.
Abhinav Bindra's Olympic gold was in which sport?|Shooting|Archery|Boxing|Wrestling|Bindra won the 10 m air rifle in 2008.
The Ajanta caves, famed for Buddhist murals, are in which state?|Maharashtra|Madhya Pradesh|Karnataka|Gujarat|Ajanta is in Maharashtra.
The Konark Sun Temple is in which Indian state?|Odisha|West Bengal|Andhra Pradesh|Tamil Nadu|Konark is a 13th-century Odisha temple.
Hampi, capital remnant of Vijayanagara, is in which state?|Karnataka|Andhra Pradesh|Tamil Nadu|Kerala|Hampi's ruins lie in Karnataka.
The Ganga is said to emerge from which glacier region?|Gangotri|Siachen|Pindari|Zemu|Gomukh on the Gangotri system is the traditional snout.
Kangchenjunga, India's highest peak, lies on the border with which country?|Nepal|Bhutan|China|Myanmar|Kangchenjunga is on the India–Nepal border.
The Siachen Glacier is in which mountain range?|Karakoram|Western Ghats|Aravalli|Satpura|Siachen is in the eastern Karakoram.
ISRO's first satellite was named after which ancient astronomer?|Aryabhata|Varahamihira|Bhaskara|Brahmagupta|Aryabhata launched in 1975.
UPI instant payments were built by which Indian organisation?|NPCI|RBI|SEBI|NITI Aayog|The National Payments Corporation of India operates UPI.
Aadhaar biometric IDs are issued by which Indian authority?|UIDAI|Election Commission|NITI Aayog|MeitY only|UIDAI issues Aadhaar numbers.
The 2008 Mumbai attacks are also referred to by which date shorthand?|26/11|9/11|7/11|13/12|The attacks began on 26 November 2008.
Dhyan Chand, a hockey legend, represented which country?|India|Pakistan|Australia|Netherlands|Dhyan Chand is an Indian hockey icon.
The Ranji Trophy is named after which cricketer-prince?|Ranjitsinhji|Pataudi|Vizianagaram|Cooch Behar|Kumar Shri Ranjitsinhji inspired the trophy name.
Viswanathan Anand became World Chess Champion representing which country?|India|Russia|USA|Germany|Anand is India's first modern world chess champion.
Saina Nehwal won Olympic bronze in which racquet sport?|Badminton|Tennis|Squash|Table tennis|Nehwal's medal was in badminton.
The French Open is played on which surface, also a soil type?|Clay|Grass|Hard court|Carpet|Roland-Garros is a clay-court major.
Wimbledon is hosted in which country that also has Lord's cricket ground?|England|France|Australia|USA|Both Wimbledon and Lord's are in England.
The 2022 football World Cup was hosted by which Gulf country?|Qatar|UAE|Saudi Arabia|Kuwait|Qatar hosted the 2022 tournament.
Pelé won World Cups for which South American country?|Brazil|Argentina|Uruguay|Chile|Pelé won in 1958, 1962 and 1970.
The Amazon rainforest is largely in which country?|Brazil|Peru|Colombia|Venezuela|Most of the Amazon basin is in Brazil.
The Nile's longest traditionally named course is associated with which continent?|Africa|Asia|Europe|South America|The Nile is an African river.
Mount Everest lies on the border of Nepal and which country?|China|India|Bhutan|Pakistan|Everest is on the Nepal–China (Tibet) border.
The Sahara is the world's largest hot desert on which continent?|Africa|Asia|Australia|North America|The Sahara spans North Africa.
The Great Barrier Reef lies off which country?|Australia|Indonesia|Philippines|New Zealand|The reef is off Queensland, Australia.
The Dead Sea's shores are in which region?|West Asia|East Africa|Southern Europe|Central Asia|The Dead Sea borders Jordan and Israel.
The Panama Canal links the Atlantic to which ocean?|Pacific|Indian|Arctic|Southern|The canal connects Atlantic and Pacific.
The Suez Canal is in which country?|Egypt|Turkey|Greece|Italy|The Suez Canal cuts through Egypt.
The Strait of Gibraltar separates Spain from which continent?|Africa|Asia|South America|Australia|It lies between Spain and Morocco.
The International Date Line runs roughly along which meridian?|180 degrees|0 degrees|90 East|45 West|The Date Line follows near 180° longitude.
Zero degrees latitude is called what?|Equator|Prime Meridian|Tropic of Cancer|Arctic Circle|The Equator is 0° latitude.
Zero degrees longitude is called what?|Prime Meridian|Equator|International Date Line|Tropic of Capricorn|The Prime Meridian runs through Greenwich.
The Tropic of Cancer passes through which Indian city famously?|Ujjain is nearby historically; the tropic crosses India|Ranchi|Shimla|Amritsar|The Tropic of Cancer crosses central India including Madhya Pradesh.
The Himalayas were formed by the collision of India with which plate?|Eurasian Plate|Pacific Plate|African Plate|Nazca Plate|The Indian plate collided with Eurasia.
Deccan Traps are vast volcanic basalts mainly in which Indian region?|Western-central India|Northeast India|Thar Desert|Coromandel Coast|The traps cover much of Maharashtra and nearby states.
The Western Ghats are a biodiversity hotspot along which Indian coast?|West coast|East coast|Himalayan foothills|Andaman coast|They run parallel to the Arabian Sea.
Chilika Lake, a famous lagoon, is in which state?|Odisha|Andhra Pradesh|Tamil Nadu|Gujarat|Chilika is in Odisha.
Loktak Lake with phumdis is in which Indian state?|Manipur|Meghalaya|Mizoram|Nagaland|Loktak is in Manipur.
The Sundarbans mangrove forest is shared by India and which country?|Bangladesh|Myanmar|Nepal|Bhutan|The Sundarbans span India and Bangladesh.
Kaziranga National Park is famed for one-horned rhinos in which state?|Assam|West Bengal|Bihar|Odisha|Kaziranga is in Assam.
Gir Forest is the last wild home of the Asiatic lion in which state?|Gujarat|Rajasthan|Madhya Pradesh|Maharashtra|Gir is in Gujarat.
Jim Corbett National Park is in which Himalayan state?|Uttarakhand|Himachal Pradesh|Sikkim|Jammu and Kashmir|Corbett is in Uttarakhand.
Ranthambore National Park is in which state?|Rajasthan|Gujarat|Madhya Pradesh|Haryana|Ranthambore is in Rajasthan.
The Thar Desert is mainly in which Indian state?|Rajasthan|Gujarat|Punjab|Haryana|The Thar covers much of western Rajasthan.
Mawsynram, among Earth's wettest places, is in which state?|Meghalaya|Assam|Arunachal Pradesh|Sikkim|Mawsynram is in Meghalaya.
Kodaikanal is a hill station in which southern state?|Tamil Nadu|Kerala|Karnataka|Andhra Pradesh|Kodaikanal is in Tamil Nadu.
Munnar's tea hills are in which state?|Kerala|Tamil Nadu|Karnataka|Goa|Munnar is in Kerala.
Coorg (Kodagu) coffee country is in which state?|Karnataka|Kerala|Tamil Nadu|Goa|Kodagu is in Karnataka.
Darjeeling tea gardens are in which state?|West Bengal|Assam|Sikkim|Tripura|Darjeeling is in West Bengal.
The Charminar is a landmark of which city?|Hyderabad|Lucknow|Bhopal|Jaipur|Charminar stands in Hyderabad.
India Gate commemorates soldiers and stands in which city?|New Delhi|Mumbai|Kolkata|Chennai|India Gate is in New Delhi.
The Gateway of India stands facing the sea in which city?|Mumbai|Chennai|Kochi|Visakhapatnam|The Gateway is on Mumbai's waterfront.
Howrah Bridge crosses which river?|Hooghly|Yamuna|Godavari|Narmada|It spans the Hooghly at Kolkata.
The Golden Temple is in which city?|Amritsar|Ludhiana|Chandigarh|Patiala|Harmandir Sahib is in Amritsar.
Meenakshi Temple is in which city of Tamil Nadu?|Madurai|Chennai|Coimbatore|Tiruchirappalli|The temple is in Madurai.
Jagannath Temple's famous rath yatra is in which city?|Puri|Bhubaneswar|Cuttack|Konark|Puri hosts the Jagannath Rath Yatra.
Somnath Temple is on the coast of which state?|Gujarat|Maharashtra|Goa|Kerala|Somnath is in Gujarat.
The Ellora caves include a monolithic Kailasa temple in which state?|Maharashtra|Karnataka|Madhya Pradesh|Telangana|Ellora is in Maharashtra.
Khajuraho temples are in which state?|Madhya Pradesh|Uttar Pradesh|Rajasthan|Bihar|Khajuraho is in Madhya Pradesh.
Sanchi Stupa is in which state?|Madhya Pradesh|Uttar Pradesh|Bihar|Odisha|Sanchi is in Madhya Pradesh.
Nalanda's ancient university ruins are in which state?|Bihar|Uttar Pradesh|Jharkhand|West Bengal|Nalanda is in Bihar.
Fatehpur Sikri was built by which Mughal emperor?|Akbar|Shah Jahan|Jahangir|Humayun|Akbar founded Fatehpur Sikri.
The Taj Mahal was commissioned by which emperor?|Shah Jahan|Akbar|Aurangzeb|Babur|Shah Jahan built it for Mumtaz Mahal.
Red Fort in Delhi was built mainly under which emperor?|Shah Jahan|Akbar|Aurangzeb|Humayun|Shah Jahan's Shahjahanabad included the Red Fort.
Qutub Minar is in which city?|Delhi|Agra|Jaipur|Lucknow|Qutub Minar stands in Delhi.
Hawa Mahal is in which pink-city capital?|Jaipur|Jodhpur|Udaipur|Bikaner|Hawa Mahal is in Jaipur.
Mysore Palace is in which state?|Karnataka|Tamil Nadu|Kerala|Andhra Pradesh|Mysuru is in Karnataka.
Victoria Memorial is a landmark of which city?|Kolkata|Mumbai|Chennai|Delhi|The memorial is in Kolkata.
Charaka is associated with which ancient Indian knowledge system?|Ayurveda|Yoga sutras|Astronomy|Architecture|Charaka is a foundational Ayurveda figure.
Sushruta is remembered as a pioneer of which field?|Surgery|Alchemy|Navigation|Music|Sushruta Samhita discusses surgical methods.
Panini is famed for a grammar of which language?|Sanskrit|Pali|Tamil|Prakrit|Panini's Ashtadhyayi describes Sanskrit.
Aryabhata proposed that Earth rotates on its axis in which civilisation?|Indian|Greek|Chinese|Mayan|Aryabhata worked in classical India.
The concept of zero as a numeral was developed in which civilisation?|Indian|Roman|Egyptian|Incan|Indian mathematicians formalised zero.
Chess is widely believed to have ancestry in which Indian game?|Chaturanga|Pachisi|Kabaddi|Gilli-danda|Chaturanga is a likely chess ancestor.
Polo has historic roots on which continent?|Asia|Europe|Africa|Australia|Polo's early history is associated with Asia.
Yoga as a philosophical system is rooted in which country?|India|Tibet|Japan|Greece|Yoga traditions developed in India.
The decimal place-value system spread from which civilisation?|Indian|Mayan|Babylonian only|Chinese only|India's place-value system spread widely.
The Indus Valley sites Harappa and Mohenjo-daro are now mainly in which country?|Pakistan|India|Afghanistan|Iran|Both major sites are in present-day Pakistan.
Dholavira, an Indus city, is in which Indian union territory/state region?|Gujarat|Rajasthan|Punjab|Haryana|Dholavira is in Gujarat's Kutch.
Lothal, an Indus port-like site, is in which state?|Gujarat|Maharashtra|Goa|Kerala|Lothal is in Gujarat.
The Kalinga war is linked to which emperor's change of heart?|Ashoka|Chandragupta|Harsha|Samudragupta|Ashoka's remorse after Kalinga is traditional history.
The iron pillar of Delhi is noted for resistance to what?|Rust|Earthquake|Flood|Fire|The pillar is famous for corrosion resistance.
Tipu Sultan ruled from which southern city?|Srirangapatna|Mysuru only|Bengaluru|Madurai|Tipu's capital was Srirangapatna.
Shivaji was crowned at which fort?|Raigad|Sinhagad|Pratapgad|Rajgad|The 1674 coronation was at Raigad.
The Vijayanagara capital is remembered through which site?|Hampi|Badami|Aihole|Pattadakal|Hampi preserves Vijayanagara ruins.
The Chola bronze tradition is especially associated with which region?|Tamil Nadu|Kerala|Odisha|Bengal|Chola bronzes are a Tamil country art.
The Khalsa was founded by which Sikh Guru?|Guru Gobind Singh|Guru Nanak|Guru Arjan|Guru Tegh Bahadur|Guru Gobind Singh founded the Khalsa in 1699.
Guru Nanak was born in which present-day country?|Pakistan|India|Bangladesh|Nepal|Nankana Sahib is in Pakistan.
The Battle of Plassey was fought in which present-day country?|India|Bangladesh|Pakistan|Myanmar|Plassey is in West Bengal, India.
The capital of British India moved from Calcutta to Delhi in which year?|1911|1905|1931|1947|The 1911 durbar announced the shift.
Rabindranath Tagore founded which university at Santiniketan?|Visva-Bharati|Jadavpur|Presidency|Calcutta University|Visva-Bharati grew from Santiniketan.
The IISc is located in which city?|Bengaluru|Mumbai|Chennai|Hyderabad|The Indian Institute of Science is in Bengaluru.
IIT Bombay is in which city?|Mumbai|Pune|Nagpur|Ahmedabad|IIT Bombay is in Powai, Mumbai.
The first IIT was established at which place?|Kharagpur|Madras|Kanpur|Delhi|IIT Kharagpur was the first.
Bhabha Atomic Research Centre is in which city?|Mumbai|Kolkata|Chennai|Hyderabad|BARC is in Trombay, Mumbai.
The Green Revolution in India is associated with which crop most famously?|Wheat|Tea|Cotton|Rubber|High-yield wheat transformed north Indian agriculture.
White Revolution in India is associated with which product?|Milk|Sugar|Cotton|Jute|Operation Flood expanded dairy cooperatives.
Blue Revolution refers to growth in which sector?|Fisheries|Aviation|IT|Steel|Blue Revolution concerns aquaculture and fisheries.
The Planning Commission was replaced in 2015 by which body?|NITI Aayog|Finance Commission|Election Commission|UPSC|NITI Aayog succeeded the Planning Commission.
GST in India was rolled out in which year?|2017|2014|2016|2019|Goods and Services Tax began on 1 July 2017.
The Right to Information Act was passed in which year?|2005|2002|2010|1995|The RTI Act is of 2005.
MGNREGA rural employment law was enacted in which year?|2005|2000|2010|1999|The 2005 Act guarantees rural wage employment.
The Indian spaceport at Sriharikota is in which state?|Andhra Pradesh|Tamil Nadu|Odisha|Kerala|SDSC SHAR is in Andhra Pradesh.
Thumba Equatorial Rocket Launching Station is in which state?|Kerala|Tamil Nadu|Karnataka|Goa|TERLS is near Thiruvananthapuram.
The first Indian in space was?|Rakesh Sharma|Kalpana Chawla|Sunita Williams|Ravish Malhotra|Rakesh Sharma flew in 1984.
Kalpana Chawla was an astronaut born in which country?|India|USA|UK|Canada|She was born in Karnal, India, and flew with NASA.
The Ramanujan number 1729 is the smallest number expressible as the sum of two cubes in how many ways?|Two|Three|Four|One|1729 = 1³+12³ = 9³+10³.
Pi is the ratio of a circle's circumference to its what?|Diameter|Radius|Area|Chord|π = C/d.
The speed of light in vacuum is approximately 3 × 10^8 of which unit?|Metres per second|Kilometres per hour|Miles per second|Knots|c ≈ 3×10^8 m/s.
Sound cannot travel through which medium?|Vacuum|Steel|Water|Air|Sound needs a material medium.
Photosynthesis in plants mainly releases which gas?|Oxygen|Nitrogen|Carbon dioxide|Hydrogen|Oxygen is a by-product of photosynthesis.
The SI unit of electric current is?|Ampere|Volt|Ohm|Watt|Current is measured in amperes.
Newton's first law is also called the law of?|Inertia|Gravitation|Action-reaction|Energy|A body remains in its state unless acted upon.
Einstein's E=mc² relates energy to?|Mass|Charge|Temperature|Volume|Energy equals mass times c squared.
The ozone layer absorbs much of which solar radiation?|Ultraviolet|Infrared|Radio|Microwave|Stratospheric ozone absorbs UV-B.
Acid rain is linked to oxides of sulfur and?|Nitrogen|Helium|Neon|Argon|SOx and NOx drive acid rain.
The hardest natural mineral on the Mohs scale is?|Diamond|Quartz|Corundum|Topaz|Diamond is 10 on the Mohs scale.
""".strip().splitlines() if "|" in x]


def others(values: list[str], keep: str, k: int = 3) -> list[str]:
    pool = [v for v in values if v != keep]
    rng = random.Random(keep + str(len(pool)))
    rng.shuffle(pool)
    out = []
    for v in pool:
        if v not in out:
            out.append(v)
        if len(out) == k:
            break
    while len(out) < k:
        out.append(f"Option {len(out)+1}")
    return out


def build_gk() -> list[dict]:
    qs: list[dict] = []
    countries = [c[0] for c in CAPITALS]
    capitals = [c[1] for c in CAPITALS]
    for i, (country, capital, continent) in enumerate(CAPITALS):
        n = i + 1
        qs.append(
            item(
                "gk",
                n,
                f"Which city is the capital of {country}?",
                capital,
                others(capitals, capital),
                f"{capital} is the capital of {country} in {continent}.",
            )
        )
    for i, (country, capital, continent) in enumerate(CAPITALS):
        n = 101 + i
        qs.append(
            item(
                "gk",
                n,
                f"{capital} is the capital of which country?",
                country,
                others(countries, country),
                f"{capital} is the capital of {country} in {continent}.",
            )
        )
    return qs[:200]


def build_sci() -> list[dict]:
    qs: list[dict] = []
    names = [e[0] for e in ELEMENTS]
    symbols = [e[1] for e in ELEMENTS]
    for i, (name, symbol, z) in enumerate(ELEMENTS):
        qs.append(
            item(
                "sci",
                i + 1,
                f"What is the chemical symbol for {name}?",
                symbol,
                others(symbols, symbol),
                f"{name} has the symbol {symbol} and atomic number {z}.",
            )
        )
    for i, (name, symbol, z) in enumerate(ELEMENTS):
        qs.append(
            item(
                "sci",
                101 + i,
                f"Which element has atomic number {z}?",
                name,
                others(names, name),
                f"Atomic number {z} is {name} ({symbol}).",
            )
        )
    return qs[:200]


def build_his() -> list[dict]:
    qs: list[dict] = []
    years = [h[0] for h in HISTORY]
    events = [h[1] for h in HISTORY]
    for i, (year, event, label) in enumerate(HISTORY):
        qs.append(
            item(
                "his",
                i + 1,
                f"In which year did this happen: {event}?",
                year,
                others(years, year),
                f"{label}: {event} ({year}).",
            )
        )
    for i, (year, event, label) in enumerate(HISTORY):
        qs.append(
            item(
                "his",
                101 + i,
                f"The year {year} is best remembered for which of these?",
                event,
                others(events, event),
                f"In {year}, {event}.",
            )
        )
    for i, (year, event, label) in enumerate(HISTORY):
        qs.append(
            item(
                "his",
                201 + i,
                f"Which year is most closely associated with this: {label}?",
                year,
                others(years, year),
                f"{label} is linked to {year}: {event}.",
            )
        )
    return qs


def build_spo() -> list[dict]:
    qs: list[dict] = []
    facts = [s[1] for s in SPORTS]
    answers = [s[2] for s in SPORTS]
    sports = [s[0] for s in SPORTS]
    for i, (sport, fact, ans) in enumerate(SPORTS[:100]):
        qs.append(
            item(
                "spo",
                i + 1,
                f"In {sport}, which is correct: {fact}?",
                "True of this sport's history",
                [
                    "It describes a Winter Olympic skating rule",
                    "It is a Formula One pit-stop regulation",
                    "It is an ancient Olympic wrestling hold",
                ]
                if False
                else others(answers, ans)
                if ans != fact
                else others(list(dict.fromkeys(answers)), ans if ans in answers else answers[0]),
                f"{fact} ({sport}: {ans}).",
            )
        )
    # Rebuild sports questions more cleanly
    qs = []
    for i, (sport, fact, ans) in enumerate(SPORTS[:100]):
        qs.append(
            item(
                "spo",
                i + 1,
                f"Which answer matches this {sport} fact: {fact}?",
                ans,
                others(list(dict.fromkeys(answers)), ans),
                f"{fact} — {ans}.",
            )
        )
    for i, (sport, fact, ans) in enumerate(SPORTS):
        qs.append(
            item(
                "spo",
                201 + i,
                f"This achievement or rule belongs to which sport: {fact}?",
                sport,
                others(list(dict.fromkeys(sports)), sport),
                f"{fact} — {sport} ({ans}).",
            )
        )
    return qs


def build_mov() -> list[dict]:
    qs: list[dict] = []
    titles = [f[0] for f in FILMS]
    years = [f[1] for f in FILMS]
    directors = [f[2] for f in FILMS]
    for i, (title, year, director) in enumerate(FILMS[:100]):
        qs.append(
            item(
                "mov",
                i + 1,
                f"Who directed the film {title}?",
                director,
                others(list(dict.fromkeys(directors)), director),
                f"{title} ({year}) was directed by {director}.",
            )
        )
    for i, (title, year, director) in enumerate(FILMS[:100]):
        qs.append(
            item(
                "mov",
                101 + i,
                f"In which year was the film {title} released?",
                year,
                others(list(dict.fromkeys(years)), year),
                f"{title} released in {year}, directed by {director}.",
            )
        )
    return qs[:200]


def build_tech() -> list[dict]:
    qs: list[dict] = []
    names = [t[0] for t in TECH]
    makers = [t[1] for t in TECH]
    years = [t[2] for t in TECH]
    for i, (name, maker, year) in enumerate(TECH[:100]):
        qs.append(
            item(
                "tech",
                i + 1,
                f"Which person, team or organisation is most closely tied to {name}?",
                maker,
                others(list(dict.fromkeys(makers)), maker),
                f"{name} is associated with {maker} ({year}).",
            )
        )
    for i, (name, maker, year) in enumerate(TECH[:100]):
        qs.append(
            item(
                "tech",
                101 + i,
                f"{name} is best placed in which period or marker?",
                year,
                others(list(dict.fromkeys(years)), year),
                f"{name} — {maker} ({year}).",
            )
        )
    return qs[:200]


def parse_fusion_line(line: str) -> tuple[str, str, list[str], str]:
    parts = line.split("|")
    prompt, correct, w1, w2, w3, expl = parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]
    return prompt, correct, [w1, w2, w3], expl


def build_fusion() -> list[dict]:
    qs: list[dict] = []
    parsed = []
    for line in FUSION:
        if len(line) >= 6:
            parsed.append(parse_fusion_line("|".join(line) if isinstance(line, tuple) else line))
        else:
            # already tuple of 6+
            parsed.append((line[0], line[1], list(line[2:5]), line[5] if len(line) > 5 else line[1]))
    # FUSION was split on | so each line is a tuple
    rows = []
    raw = Path(__file__).read_text(encoding="utf-8")
    # Use the FUSION list as tuples from split
    for row in FUSION:
        if len(row) < 6:
            continue
        rows.append(row)
    for i, row in enumerate(rows):
        prompt, correct, w1, w2, w3, expl = row[0], row[1], row[2], row[3], row[4], row[5]
        qs.append(item("fusion", i + 1, prompt, correct, [w1, w2, w3], expl))
    continents = [c[2] for c in CAPITALS]
    for i, (country, capital, continent) in enumerate(CAPITALS):
        qs.append(
            item(
                "fusion",
                300 + i,
                f"{country} lies on which continent?",
                continent,
                others(list(dict.fromkeys(continents)), continent),
                f"{country}, whose capital is {capital}, is in {continent}.",
            )
        )
    return qs


def build_nexus() -> list[dict]:
    """Harder mixed bank: atomic trivia, institutions, firsts, measures."""
    extra = [
        ("Which Indian article of the Constitution deals with equality before the law?", "Article 14", ["Article 19", "Article 21", "Article 32"], "Article 14 guarantees equality before the law and equal protection of the laws."),
        ("Which Fundamental Right article protects life and personal liberty?", "Article 21", ["Article 14", "Article 15", "Article 19"], "Article 21 is the right to life and personal liberty."),
        ("The Directive Principles of State Policy are in which part of the Constitution?", "Part IV", ["Part III", "Part V", "Part II"], "DPSPs are in Part IV."),
        ("Who was the chairman of the Drafting Committee of the Indian Constitution?", "B. R. Ambedkar", ["Jawaharlal Nehru", "Rajendra Prasad", "Sardar Patel"], "Ambedkar chaired the Drafting Committee."),
        ("Who was the first President of India?", "Rajendra Prasad", ["S. Radhakrishnan", "Zakir Husain", "V. V. Giri"], "Dr Rajendra Prasad was the first President."),
        ("Who was the first Prime Minister of India?", "Jawaharlal Nehru", ["Lal Bahadur Shastri", "Sardar Patel", "Rajendra Prasad"], "Nehru was India's first Prime Minister."),
        ("The Supreme Court of India sits in which city?", "New Delhi", ["Mumbai", "Kolkata", "Prayagraj"], "The Supreme Court is in New Delhi."),
        ("The Reserve Bank of India was established in which year?", "1935", ["1947", "1950", "1921"], "RBI began operations in 1935."),
        ("Who issues the ₹1 note in India?", "Government of India", ["RBI", "NITI Aayog", "SEBI"], "The ₹1 note is issued by the Government of India."),
        ("SEBI regulates which Indian market?", "Securities market", ["Telecom", "Aviation", "Insurance only"], "SEBI is the securities-market regulator."),
        ("IRDAI regulates which sector in India?", "Insurance", ["Banking", "Capital markets", "Pensions only"], "IRDAI regulates insurance."),
        ("The Finance Commission is constituted under which article?", "Article 280", ["Article 356", "Article 370", "Article 32"], "Article 280 provides for the Finance Commission."),
        ("Lok Sabha's maximum strength is constitutionally around?", "550", ["250", "750", "350"], "The Constitution allows up to 550 elected members plus a possible two nominated."),
        ("Rajya Sabha is a?", "Permanent house", ["Dissolved every 5 years", "Dissolved every 6 years", "Nominated only"], "Rajya Sabha is a continuing chamber with staggered terms."),
        ("The Election Commission of India is a?", "Constitutional body", ["Statutory board", "Cabinet committee", "NGO"], "The ECI is a constitutional body."),
        ("NITI Aayog was formed in which year?", "2015", ["2014", "2016", "2010"], "NITI Aayog replaced the Planning Commission in 2015."),
        ("The Tropic of Cancer does NOT pass through which state?", "Kerala", ["Gujarat", "Madhya Pradesh", "West Bengal"], "Kerala lies south of the Tropic of Cancer."),
        ("Which is the longest river entirely in India?", "Ganga is international; Godavari is the longest in India among peninsular — wait", ["Yamuna", "Narmada", "Mahanadi"], "placeholder"),
    ]
    # Fix the bad one and generate 200 solid nexus questions
    qs: list[dict] = []
    nexus_rows = [
        ("Which Indian constitutional article guarantees equality before the law?", "Article 14", "Article 19", "Article 21", "Article 32", "Article 14 guarantees equality before the law."),
        ("Which article protects life and personal liberty in India?", "Article 21", "Article 14", "Article 15", "Article 19", "Article 21 is the right to life and personal liberty."),
        ("Who chaired the Drafting Committee of the Constitution of India?", "B. R. Ambedkar", "Jawaharlal Nehru", "Rajendra Prasad", "Sardar Patel", "Ambedkar chaired the Drafting Committee."),
        ("Who was the first President of India?", "Rajendra Prasad", "S. Radhakrishnan", "Zakir Husain", "V. V. Giri", "Dr Rajendra Prasad was the first President."),
        ("Who was the first Vice-President of India?", "S. Radhakrishnan", "Zakir Husain", "G. S. Pathak", "Hamid Ansari", "S. Radhakrishnan was the first Vice-President."),
        ("Who was the first Prime Minister of India?", "Jawaharlal Nehru", "Lal Bahadur Shastri", "Sardar Patel", "Rajendra Prasad", "Nehru led the first cabinet."),
        ("Who was India's first woman Prime Minister?", "Indira Gandhi", "Sarojini Naidu", "Pratibha Patil", "Sushma Swaraj", "Indira Gandhi became PM in 1966."),
        ("Who was India's first woman President?", "Pratibha Patil", "Indira Gandhi", "Sonia Gandhi", "Meira Kumar", "Pratibha Patil served 2007–2012."),
        ("The Supreme Court of India is in which city?", "New Delhi", "Mumbai", "Kolkata", "Prayagraj", "The Supreme Court sits in New Delhi."),
        ("RBI was established in which year?", "1935", "1947", "1950", "1921", "The Reserve Bank of India began in 1935."),
        ("The Indian rupee symbol ₹ was adopted in which year?", "2010", "2000", "2015", "1996", "The ₹ design by D. Udaya Kumar was adopted in 2010."),
        ("Who designed the Indian rupee symbol?", "D. Udaya Kumar", "R. K. Laxman", "Satyajit Ray", "Charles Correa", "Udaya Kumar's design was selected in 2010."),
        ("SEBI headquarters are in which city?", "Mumbai", "Delhi", "Kolkata", "Hyderabad", "SEBI is based in Mumbai."),
        ("The Sensex is the benchmark index of which exchange?", "BSE", "NSE", "MCX", "NCDEX", "Sensex tracks the Bombay Stock Exchange."),
        ("Nifty 50 is the benchmark of which exchange?", "NSE", "BSE", "MSE", "CSE", "Nifty 50 is the National Stock Exchange index."),
        ("India's financial year currently begins in which month?", "April", "January", "July", "October", "The Union financial year starts 1 April."),
        ("GST in India subsumed many earlier taxes from which year?", "2017", "2014", "2016", "2018", "GST launched on 1 July 2017."),
        ("The Finance Commission is provided under which article?", "Article 280", "Article 356", "Article 370", "Article 32", "Article 280 creates the Finance Commission."),
        ("President's Rule in a state is associated with which article?", "Article 356", "Article 352", "Article 360", "Article 365", "Article 356 allows state emergency provisions."),
        ("A national emergency is dealt with under which article?", "Article 352", "Article 356", "Article 360", "Article 368", "Article 352 covers national emergency."),
        ("The Election Commission is mentioned in which article?", "Article 324", "Article 326", "Article 148", "Article 76", "Article 324 vests elections in the ECI."),
        ("CAG of India is provided under which article?", "Article 148", "Article 76", "Article 124", "Article 280", "Article 148 establishes the CAG."),
        ("Attorney General of India is under which article?", "Article 76", "Article 78", "Article 148", "Article 124", "Article 76 provides for the Attorney General."),
        ("The official language of the Union is described in which article?", "Article 343", "Article 351", "Article 29", "Article 21A", "Article 343 makes Hindi in Devanagari the Union official language, with English continuing."),
        ("Fundamental Duties were added by which amendment?", "42nd", "44th", "73rd", "86th", "The 42nd Amendment added Fundamental Duties."),
        ("The 73rd Amendment is about which institutions?", "Panchayats", "Municipalities", "Cooperatives", "Tribunals", "The 73rd Amendment constitutionalised Panchayati Raj."),
        ("The 74th Amendment is about which institutions?", "Municipalities", "Panchayats", "High Courts", "Lok Adalats", "The 74th Amendment covers urban local bodies."),
        ("Right to Education became a Fundamental Right by which amendment?", "86th", "42nd", "44th", "73rd", "The 86th Amendment inserted Article 21A."),
        ("How many Schedules does the Constitution of India currently have?", "12", "10", "8", "22", "There are 12 Schedules."),
        ("How many parts did the original Constitution of India have?", "22", "12", "25", "8", "The original Constitution had 22 parts."),
        ("The Constituent Assembly first met in which year?", "1946", "1947", "1950", "1945", "The Assembly first met in December 1946."),
        ("The Constitution was adopted on which date?", "26 November 1949", "26 January 1950", "15 August 1947", "26 January 1949", "26 November is Constitution Day."),
        ("Republic Day marks which event?", "Constitution coming into force", "Independence from Britain", "Quit India", "Dandi March", "26 January 1950 is when the Constitution took effect."),
        ("Who was the first Chief Justice of India?", "H. J. Kania", "M. C. Mahajan", "M. Patanjali Sastri", "A. K. Sarkar", "Justice Kania was the first CJI."),
        ("The National Human Rights Commission was set up in which year?", "1993", "1950", "1976", "2005", "NHRC was established in 1993."),
        ("NITI Aayog replaced which body?", "Planning Commission", "Finance Commission", "Law Commission", "UPSC", "The Planning Commission was replaced in 2015."),
        ("Who is the ex-officio Chairperson of NITI Aayog?", "Prime Minister", "President", "Finance Minister", "Home Minister", "The PM chairs NITI Aayog."),
        ("The Tropic of Cancer does NOT pass through which Indian state?", "Kerala", "Rajasthan", "Gujarat", "Tripura", "Kerala is south of the Tropic of Cancer."),
        ("Which is the easternmost state of India?", "Arunachal Pradesh", "Nagaland", "Manipur", "Mizoram", "Kibithu/Kibithoo area of Arunachal is at India's eastern extremity."),
        ("Which is the westernmost state of India?", "Gujarat", "Rajasthan", "Maharashtra", "Goa", "Guhar Moti in Gujarat is often cited as the westernmost village."),
        ("Indira Point is at the southern tip of which island group?", "Andaman and Nicobar", "Lakshadweep", "Sundarbans", "Diu", "Indira Point is in Great Nicobar."),
        ("Kanyakumari is the southern tip of which Indian landmass?", "Mainland India", "Andaman", "Lakshadweep", "Sikkim", "Kanyakumari is the mainland's southern tip."),
        ("The Radcliffe Line divided India and?", "Pakistan", "China", "Myanmar", "Nepal", "The 1947 boundary was drawn by Cyril Radcliffe."),
        ("The McMahon Line is associated with India's border with?", "China", "Pakistan", "Bangladesh", "Nepal", "The McMahon Line relates to the India–Tibet/China sector."),
        ("The Durand Line is between Pakistan and?", "Afghanistan", "India", "Iran", "China", "The Durand Line dates from 1893."),
        ("India's standard time is based on which longitude?", "82.5° E", "75° E", "90° E", "80° E", "IST is UTC+5:30, centred near 82.5° E."),
        ("Which city is close to the IST reference longitude?", "Prayagraj / Mirzapur region", "Mumbai", "Chennai", "Ahmedabad", "The 82.5° E meridian passes near Mirzapur."),
        ("The Standard Meridian of India passes near which state as well?", "Uttar Pradesh", "Goa", "Kerala", "Gujarat", "82.5° E crosses UP among other areas."),
        ("Chilika is Asia's largest brackish water lagoon in which state?", "Odisha", "Andhra Pradesh", "Tamil Nadu", "Gujarat", "Chilika Lake is in Odisha."),
        ("Wular Lake is in which Union Territory?", "Jammu and Kashmir", "Ladakh", "Puducherry", "Delhi", "Wular is in Jammu and Kashmir."),
        ("Pulicat Lake is shared by Tamil Nadu and which state?", "Andhra Pradesh", "Kerala", "Karnataka", "Odisha", "Pulicat lies on the TN–AP border."),
        ("The highest peak in South India is?", "Anamudi", "Doddabetta", "Mullayanagiri", "Agasthyamalai", "Anamudi in Kerala is the highest in the Western Ghats/South India."),
        ("Guru Shikhar is the highest peak of which range?", "Aravalli", "Satpura", "Vindhya", "Nilgiri", "Guru Shikhar is in the Aravallis, Rajasthan."),
        ("Dhupgarh is the highest peak of which range?", "Satpura", "Aravalli", "Western Ghats", "Eastern Ghats", "Dhupgarh is in the Satpura range."),
        ("The source of the Narmada is traditionally?", "Amarkantak", "Gangotri", "Mahabaleshwar", "Trimbakeshwar", "Narmada rises near Amarkantak."),
        ("The Godavari is often called the Dakshina Ganga and rises in?", "Trimbakeshwar", "Amarkantak", "Mahabaleshwar", "Coorg", "Godavari rises at Trimbak in Maharashtra."),
        ("Kaveri rises in which hills?", "Brahmagiri (Kodagu)", "Nilgiris only", "Cardamom", "Shevaroy", "The Kaveri rises in Kodagu, Karnataka."),
        ("The Krishna river rises near which place?", "Mahabaleshwar", "Trimbak", "Amarkantak", "Coorg", "Krishna rises near Mahabaleshwar."),
        ("Hirakud Dam is built on which river?", "Mahanadi", "Godavari", "Krishna", "Narmada", "Hirakud is on the Mahanadi in Odisha."),
        ("Bhakra Nangal Dam is on which river?", "Sutlej", "Beas", "Ravi", "Yamuna", "Bhakra is on the Sutlej."),
        ("Tehri Dam is on which river?", "Bhagirathi", "Alaknanda", "Yamuna", "Gomti", "Tehri is on the Bhagirathi in Uttarakhand."),
        ("Sardar Sarovar Dam is on which river?", "Narmada", "Tapi", "Mahi", "Sabarmati", "Sardar Sarovar is a Narmada project."),
        ("Nagarjuna Sagar Dam is on which river?", "Krishna", "Godavari", "Pennar", "Tungabhadra", "Nagarjuna Sagar is on the Krishna."),
        ("The first nuclear reactor in India was?", "Apsara", "CIRUS", "Dhruva", "Kamini", "Apsara at Trombay went critical in 1956."),
        ("India's first satellite launch vehicle success is associated with?", "SLV-3", "ASLV", "PSLV", "GSLV", "SLV-3 launched Rohini in 1980."),
        ("PSLV is known as ISRO's workhorse for which orbits mainly?", "Sun-synchronous / polar", "GEO only", "Lunar only", "Interstellar", "PSLV has launched many polar satellites."),
        ("GSLV Mk III / LVM3 launched Chandrayaan-2 from which port?", "Sriharikota", "Thumba", "Balasore", "Wheeler Island", "GSLV Mk III flew from SDSC SHAR."),
        ("NAVIC is India's?", "Regional navigation satellite system", "Weather balloon network", "Undersea cable", "Postal code system", "NavIC is an independent regional GNSS."),
        ("GAGAN is a satellite-based augmentation system for?", "Aviation navigation", "Farm loans", "Railway tickets", "Census", "GAGAN supports GPS for Indian airspace."),
        ("The first Indian to receive the Bharat Ratna was among?", "C. Rajagopalachari, S. Radhakrishnan and C. V. Raman (1954)", "Only Gandhi", "Only Nehru", "Only Tagore", "The first Bharat Ratnas were awarded in 1954."),
        ("Param Vir Chakra is awarded for?", "Highest military gallantry", "Longest civil service", "Sports gold", "Science discovery", "PVC is India's highest wartime gallantry award."),
        ("Ashoka Chakra is awarded for?", "Peacetime gallantry", "Wartime command", "Literary work", "Export growth", "Ashoka Chakra is the highest peacetime gallantry award."),
        ("The Dadasaheb Phalke Award honours contribution to?", "Indian cinema", "Cricket", "Classical dance only", "Architecture", "It is Indian cinema's highest award."),
        ("The Jnanpith Award is for?", "Literature", "Music", "Science", "Sports", "Jnanpith honours Indian literature."),
        ("The Ramon Magsaysay Award originated in which country?", "Philippines", "India", "Japan", "USA", "It commemorates a Philippine president."),
        ("Amartya Sen won the Nobel Prize in which field?", "Economics", "Peace", "Literature", "Physics", "Sen received the 1998 economics prize."),
        ("Kailash Satyarthi won the Nobel Prize in which field?", "Peace", "Economics", "Literature", "Chemistry", "He shared the 2014 Peace Prize."),
        ("Venkatraman Ramakrishnan won the Nobel Prize in?", "Chemistry", "Physics", "Medicine only", "Economics", "He shared the 2009 chemistry prize for ribosome work."),
        ("Hargobind Khorana won the Nobel Prize in?", "Physiology or Medicine", "Physics", "Chemistry only", "Peace", "Khorana shared the 1968 medicine prize."),
        ("Subrahmanyan Chandrasekhar won the Nobel Prize in?", "Physics", "Chemistry", "Economics", "Peace", "He shared the 1983 physics prize."),
        ("The Chandrasekhar limit concerns?", "White dwarf mass", "Black hole charge", "Neutron half-life", "Solar flares", "Above ~1.4 solar masses a white dwarf is unstable."),
        ("A light-year measures?", "Distance", "Time", "Mass", "Luminosity only", "A light-year is the distance light travels in one year."),
        ("Parsec is a unit of?", "Astronomical distance", "Time", "Mass", "Temperature", "One parsec is about 3.26 light-years."),
        ("The Hubble constant relates to?", "Expansion of the universe", "Earth's magnetic field", "Ocean tides", "Plate speed", "It measures the rate of cosmic expansion."),
        ("CMB radiation is evidence for?", "Big Bang", "Steady State only", "Geocentrism", "Flat Earth", "The cosmic microwave background supports the Big Bang."),
        ("Kepler's laws describe?", "Planetary motion", "Electric circuits", "Chemical bonds", "Genetics", "Kepler described elliptical planetary orbits."),
        ("Newton's law of universal gravitation says force is inversely proportional to?", "Distance squared", "Distance", "Mass squared", "Time", "F ∝ 1/r²."),
        ("Escape velocity from Earth is about?", "11.2 km/s", "3 km/s", "30 km/s", "1 km/s", "About 11.2 kilometres per second from Earth's surface."),
        ("The SI unit of force is?", "Newton", "Joule", "Watt", "Pascal", "1 N = 1 kg·m/s²."),
        ("The SI unit of energy is?", "Joule", "Newton", "Watt", "Volt", "The joule is the SI energy unit."),
        ("The SI unit of power is?", "Watt", "Joule", "Newton", "Ampere", "Power is energy per time: watt."),
        ("The SI unit of pressure is?", "Pascal", "Bar only", "Torr only", "Atmosphere only", "Pascal is the SI pressure unit."),
        ("Absolute zero is?", "0 kelvin", "0 celsius", "0 fahrenheit", "−100 celsius", "0 K is the lowest thermodynamic temperature."),
        ("Water boils at sea level at?", "100°C", "90°C", "80°C", "120°C", "At 1 atm, water boils at 100°C."),
        ("Normal human body temperature is about?", "37°C", "27°C", "47°C", "32°C", "About 37°C is typical core temperature."),
        ("pH 7 is?", "Neutral", "Strong acid", "Strong base", "Undefined", "Pure water at 25°C is pH 7."),
        ("The most abundant gas in Earth's atmosphere is?", "Nitrogen", "Oxygen", "Carbon dioxide", "Argon", "Nitrogen is about 78% of dry air."),
        ("Photosynthesis occurs mainly in which plant organelle?", "Chloroplast", "Mitochondrion", "Nucleus", "Ribosome", "Chloroplasts contain chlorophyll."),
        ("Mitochondria are known as the cell's?", "Powerhouse", "Brain", "Skeleton", "Packaging centre", "They generate most ATP."),
        ("DNA's double helix model is credited to Watson, Crick and?", "Rosalind Franklin's data / Wilkins", "Mendel only", "Darwin only", "Linnaeus", "Franklin's X-ray work was crucial; Wilkins also shared the prize."),
        ("Blood group AB is called a universal?", "Recipient (ABO)", "Donor", "Neither", "Plasma only", "AB people can receive A, B, AB and O red cells in the ABO system."),
        ("Blood group O negative is often called a universal?", "Donor (red cells)", "Recipient", "Plasma donor only", "Neither", "O negative red cells are widely usable in emergencies."),
        ("Insulin is produced in which organ?", "Pancreas", "Liver", "Kidney", "Spleen", "Beta cells of the pancreas secrete insulin."),
        ("The largest organ of the human body is?", "Skin", "Liver", "Lung", "Intestine", "Skin is the largest organ by surface."),
        ("The femur is the longest bone in the?", "Thigh", "Arm", "Spine", "Foot", "The femur is the thigh bone."),
        ("Haemoglobin contains which metal?", "Iron", "Copper", "Zinc", "Magnesium", "Haem groups bind iron."),
        ("Vitamin C deficiency causes?", "Scurvy", "Rickets", "Beriberi", "Night blindness", "Scurvy is from lack of ascorbic acid."),
        ("Vitamin D deficiency is linked to?", "Rickets / osteomalacia", "Scurvy", "Pellagra", "Beriberi", "Vitamin D aids calcium metabolism."),
        ("Goitre can result from deficiency of?", "Iodine", "Iron", "Calcium", "Vitamin A", "Iodine is needed for thyroid hormones."),
        ("Malaria is transmitted by which mosquito genus?", "Anopheles", "Aedes", "Culex", "Phlebotomus", "Anopheles mosquitoes transmit malaria."),
        ("Dengue is transmitted mainly by?", "Aedes aegypti", "Anopheles", "Sandfly", "Tsetse", "Aedes mosquitoes spread dengue."),
        ("Tuberculosis is caused by?", "Mycobacterium tuberculosis", "Plasmodium", "HIV", "Vibrio cholerae", "TB is a bacterial infection."),
        ("COVID-19 is caused by which virus?", "SARS-CoV-2", "Influenza A only", "HIV", "Ebola", "SARS-CoV-2 caused the COVID-19 pandemic."),
        ("The WHO is headquartered in which city?", "Geneva", "New York", "Paris", "Rome", "WHO HQ is in Geneva."),
        ("The UN headquarters is in which city?", "New York", "Geneva", "Vienna", "Nairobi", "The UN HQ is in New York."),
        ("UNESCO is headquartered in which city?", "Paris", "Rome", "Geneva", "London", "UNESCO is in Paris."),
        ("FAO is headquartered in which city?", "Rome", "Paris", "Geneva", "Vienna", "The Food and Agriculture Organization is in Rome."),
        ("The International Court of Justice sits in which city?", "The Hague", "New York", "Geneva", "Brussels", "The ICJ is in The Hague."),
        ("NATO was founded in which year?", "1949", "1945", "1955", "1961", "The North Atlantic Treaty was signed in 1949."),
        ("The European Union's euro currency began circulating coins/notes in?", "2002", "1993", "1999 notes only wait — coins 2002", "2004", "Euro cash arrived in 2002."),
        ("The World Bank is headquartered in?", "Washington, D.C.", "New York", "London", "Basel", "The World Bank Group is in Washington."),
        ("The IMF is headquartered in?", "Washington, D.C.", "Geneva", "Paris", "Frankfurt", "The IMF is in Washington, D.C."),
        ("BRICS originally included Brazil, Russia, India, China and?", "South Africa", "Singapore", "Spain", "Sweden", "South Africa joined in 2010."),
        ("SAARC headquarters are in which city?", "Kathmandu", "New Delhi", "Islamabad", "Dhaka", "SAARC HQ is in Kathmandu."),
        ("ASEAN headquarters are in which city?", "Jakarta", "Bangkok", "Manila", "Singapore", "ASEAN Secretariat is in Jakarta."),
        ("The African Union headquarters are in?", "Addis Ababa", "Nairobi", "Lagos", "Cairo", "AU HQ is in Addis Ababa."),
        ("The G20 is a forum of major?", "Economies", "Armies", "Universities", "Religions", "G20 brings together large economies."),
        ("India hosted the G20 Leaders' Summit in which year?", "2023", "2019", "2020", "2024", "India hosted in 2023; the New Delhi summit was in September."),
        ("The Non-Aligned Movement was shaped at which 1955 conference?", "Bandung", "Yalta", "Potsdam", "Bretton Woods", "The Bandung Conference was a NAM landmark."),
        ("Panchsheel is associated with relations between India and?", "China", "USA", "USSR only", "UK", "The Five Principles were articulated in the 1950s India–China context."),
        ("The Simla Agreement of 1972 was between India and?", "Pakistan", "China", "Bangladesh only", "Sri Lanka", "Indira Gandhi and Zulfikar Ali Bhutto signed at Shimla."),
        ("The Tashkent Agreement of 1966 was between India and?", "Pakistan", "China", "USA", "USSR as party only", "It followed the 1965 war; the USSR hosted."),
        ("Pokhran-II nuclear tests were in which year?", "1998", "1974", "1991", "2008", "Operation Shakti was in May 1998."),
        ("The Civil Nuclear Deal with the US was signed in which decade?", "2000s", "1990s", "1980s", "2010s only", "The 123 Agreement era was 2005–2008."),
        ("India became a republic in which year?", "1950", "1947", "1949", "1952", "26 January 1950."),
        ("How many states does India have currently (after J&K reorganisation)?", "28", "29", "25", "30", "28 states and 8 Union Territories."),
        ("How many Union Territories does India have currently?", "8", "7", "9", "6", "There are 8 UTs after 2019–2020 changes."),
        ("The newest Union Territories created in 2019 include?", "Jammu & Kashmir and Ladakh", "Telangana and Andhra", "Uttarakhand and Jharkhand", "Goa and Daman", "The former state of J&K was reorganised into two UTs."),
        ("Telangana was formed in which year?", "2014", "2000", "2019", "2001", "Telangana was created on 2 June 2014."),
        ("Chhattisgarh was formed in which year?", "2000", "2001", "2014", "1999", "Chhattisgarh, Uttarakhand and Jharkhand were formed in 2000."),
        ("Goa became an Indian state in which year?", "1987", "1961", "1975", "1950", "Goa was liberated in 1961 and became a state in 1987."),
        ("Sikkim joined the Indian Union in which year?", "1975", "1947", "1962", "1987", "Sikkim became a state in 1975."),
        ("The official national anthem of India is?", "Jana Gana Mana", "Vande Mataram", "Saare Jahan Se Achha", "Jhanda Uncha Rahe Hamara", "Jana Gana Mana is the national anthem; Vande Mataram is the national song."),
        ("The national song of India is?", "Vande Mataram", "Jana Gana Mana", "Ae Mere Watan", "Mile Sur", "Vande Mataram is the national song."),
        ("The national animal of India is?", "Bengal tiger", "Asiatic lion", "Elephant", "Peacock", "The tiger is the national animal."),
        ("The national bird of India is?", "Indian peafowl", "Sparrow", "Eagle", "Parrot", "The peacock is the national bird."),
        ("The national flower of India is?", "Lotus", "Rose", "Marigold", "Jasmine", "Lotus is the national flower."),
        ("The national tree of India is often listed as?", "Banyan", "Neem", "Peepal only", "Teak", "The banyan is widely listed as the national tree."),
        ("The national fruit of India is?", "Mango", "Banana", "Apple", "Guava", "Mango is the national fruit."),
        ("The national aquatic animal of India is?", "Ganges river dolphin", "Olive ridley", "Whale shark", "Gharial", "The Ganges river dolphin holds that status."),
        ("The national heritage animal of India is?", "Elephant", "Tiger", "Lion", "Rhino", "The elephant is the national heritage animal."),
        ("The Indian national calendar (Saka) begins the year in?", "Chaitra", "Vaishakha", "Kartika", "Pausha", "Chaitra is the first month of the Saka calendar."),
        ("Which script is used for the official Union Hindi?", "Devanagari", "Perso-Arabic", "Roman only", "Grantha", "Article 343 specifies Devanagari."),
        ("The first war of Indian independence is a name often given to?", "1857 uprising", "1942 Quit India", "1919 Rowlatt satyagraha", "1930 Salt March", "1857 is also called the First War of Independence."),
        ("The Indian National Army was associated with which leader?", "Subhas Chandra Bose", "Bhagat Singh", "Lala Lajpat Rai", "Bal Gangadhar Tilak", "Bose led the INA in the 1940s."),
        ("Bhagat Singh was hanged in which year?", "1931", "1929", "1942", "1919", "Bhagat Singh, Rajguru and Sukhdev were executed in 1931."),
        ("Chandra Shekhar Azad died at which park in Allahabad?", "Alfred Park (Chandrashekhar Azad Park)", "Company Garden", "Ramna Park", "Cubbon Park", "He died in a shootout at Alfred Park in 1931."),
        ("The slogan 'Jai Jawan Jai Kisan' is associated with?", "Lal Bahadur Shastri", "Nehru", "Indira Gandhi", "VP Singh", "Shastri gave the slogan during the 1960s."),
        ("'Garibi Hatao' was a slogan of which leader?", "Indira Gandhi", "Rajiv Gandhi", "Morarji Desai", "Charan Singh", "It was Indira Gandhi's 1971 campaign slogan."),
        ("Operation Flood is associated with which Indian leader of cooperatives?", "Verghese Kurien", "M. S. Swaminathan", "Norman Borlaug", "C. Subramaniam", "Kurien led the dairy cooperative movement."),
        ("The Green Revolution in India is associated with which agronomist?", "M. S. Swaminathan", "Verghese Kurien", "Homi Bhabha", "Vikram Sarabhai", "Swaminathan is called the father of the Green Revolution in India."),
        ("ISRO's founding father is often named as?", "Vikram Sarabhai", "Homi Bhabha", "A. P. J. Abdul Kalam", "Satish Dhawan", "Sarabhai is regarded as the father of the Indian space programme."),
        ("Homi J. Bhabha is associated with which Indian programme?", "Nuclear", "Green Revolution", "White Revolution", "Navy only", "Bhabha architected India's nuclear research."),
        ("A. P. J. Abdul Kalam is known as the Missile Man of India for work at?", "DRDO / ISRO", "RBI", "ICAR", "AIIMS", "Kalam led missile programmes including Agni and Prithvi."),
        ("The Polar Satellite Launch Vehicle first succeeded in which decade?", "1990s", "1970s", "1960s", "2010s", "PSLV's first success was in 1994."),
        ("Chandrayaan-1 discovered evidence of which resource on the Moon?", "Water molecules / hydroxyl", "Petroleum", "Helium-only cities", "Forests", "Moon Mineralogy Mapper data indicated lunar water/hydroxyl."),
        ("Mangalyaan reached Mars orbit in which year?", "2014", "2013", "2016", "2011", "MOM entered orbit on 24 September 2014."),
        ("Aditya-L1 studies which object?", "The Sun", "Mars", "Venus", "Jupiter", "Aditya-L1 is at the Sun–Earth L1 point."),
        ("Gaganyaan is ISRO's programme to send?", "Indian astronauts to low Earth orbit", "A rover to Venus", "A telescope to L2 only", "A submarine", "Gaganyaan is India's human spaceflight programme."),
        ("The Indian Navy's aircraft carrier INS Vikramaditya was originally from?", "Russia", "UK", "USA", "France", "It was the former Admiral Gorshkov."),
        ("INS Vikrant (new) was commissioned as India's?", "Indigenous aircraft carrier", "Nuclear submarine", "Destroyer", "Frigate only", "IAC-1 Vikrant was commissioned in 2022."),
        ("India's first nuclear submarine was?", "INS Arihant", "INS Chakra", "INS Sindhughosh", "INS Kalvari", "Arihant is the lead SSBN."),
        ("The Border Roads Organisation functions under which ministry?", "Defence", "Home", "Road Transport only", "External Affairs", "BRO is under the Ministry of Defence."),
        ("CRPF is a force under which ministry?", "Home Affairs", "Defence", "Finance", "Railways", "Central Armed Police Forces are under MHA."),
        ("The Indian Coast Guard was established in which year?", "1978", "1947", "1965", "1991", "The Coast Guard Act created it in 1978."),
        ("Which city is called the Silicon Valley of India?", "Bengaluru", "Hyderabad", "Pune", "Gurugram", "Bengaluru is India's principal IT hub nickname."),
        ("Hyderabad's IT corridor is often called?", "Cyberabad", "Cyberjaya", "Silicon Coast only", "Technopark", "Cyberabad refers to Hyderabad's IT areas."),
        ("Technopark is a major IT campus in which city?", "Thiruvananthapuram", "Kochi", "Kozhikode", "Bengaluru", "Technopark is in Thiruvananthapuram."),
        ("Gift City is in which Indian state?", "Gujarat", "Maharashtra", "Karnataka", "Telangana", "GIFT City is near Gandhinagar."),
        ("The first metro rail in India began in which city?", "Kolkata", "Delhi", "Mumbai", "Chennai", "Kolkata Metro opened in 1984."),
        ("Delhi Metro Rail Corporation's first line opened in?", "2002", "1984", "2010", "1997", "Delhi Metro's first section opened in 2002."),
        ("India's first bullet-train project is between Ahmedabad and?", "Mumbai", "Delhi", "Pune", "Surat only", "The MAHSR corridor links Mumbai and Ahmedabad."),
        ("The Golden Quadrilateral highway network connects Delhi, Mumbai, Chennai and?", "Kolkata", "Hyderabad", "Bengaluru", "Pune", "Those four metros are the corners."),
        ("NH 44 (old NH 7 stretch fame) is among India's?", "Longest north–south highways", "Only coastal roads", "Only border roads", "Only city ring roads", "NH 44 runs Srinagar–Kanyakumari."),
        ("The Konkan Railway connects Maharashtra, Goa and?", "Karnataka", "Kerala", "Tamil Nadu", "Gujarat", "Konkan Railway runs through Karnataka as well."),
        ("Vande Bharat Express is a type of?", "Semi-high-speed train", "Freight corridor only", "Metro", "Tram", "It is an Indian Railways EMU trainset."),
        ("Dedicated Freight Corridors are projects of which organisation?", "Indian Railways / DFCCIL", "NHAI only", "AAI", "IWAI only", "DFCCIL builds the eastern and western freight corridors."),
        ("The Statue of Unity depicts which leader?", "Sardar Vallabhbhai Patel", "Gandhi", "Nehru", "Ambedkar", "It stands on the Narmada in Gujarat."),
        ("The Statue of Unity is in which state?", "Gujarat", "Maharashtra", "Madhya Pradesh", "Rajasthan", "It is at Kevadia, Gujarat."),
        ("India's tallest statue of Ramanuja (Statue of Equality) is in which state?", "Telangana", "Tamil Nadu", "Karnataka", "Andhra Pradesh", "It is near Hyderabad, Telangana."),
        ("Amaravati is the planned capital region associated with which state?", "Andhra Pradesh", "Telangana", "Karnataka", "Odisha", "Amaravati is Andhra Pradesh's planned capital."),
        ("Dispur is the capital of which state?", "Assam", "Meghalaya", "Nagaland", "Tripura", "Dispur is Assam's capital."),
        ("Itanagar is the capital of which state?", "Arunachal Pradesh", "Nagaland", "Manipur", "Mizoram", "Itanagar is Arunachal's capital."),
        ("Kohima is the capital of which state?", "Nagaland", "Manipur", "Mizoram", "Meghalaya", "Kohima is Nagaland's capital."),
        ("Aizawl is the capital of which state?", "Mizoram", "Nagaland", "Tripura", "Manipur", "Aizawl is Mizoram's capital."),
        ("Agartala is the capital of which state?", "Tripura", "Assam", "Manipur", "Meghalaya", "Agartala is Tripura's capital."),
        ("Imphal is the capital of which state?", "Manipur", "Nagaland", "Mizoram", "Sikkim", "Imphal is Manipur's capital."),
        ("Shillong is the capital of which state?", "Meghalaya", "Assam", "Sikkim", "Arunachal Pradesh", "Shillong is Meghalaya's capital."),
        ("Gangtok is the capital of which state?", "Sikkim", "Meghalaya", "Arunachal Pradesh", "Tripura", "Gangtok is Sikkim's capital."),
        ("Raipur is the capital of which state?", "Chhattisgarh", "Jharkhand", "Odisha", "Madhya Pradesh", "Raipur is Chhattisgarh's capital."),
        ("Ranchi is the capital of which state?", "Jharkhand", "Bihar", "Chhattisgarh", "Odisha", "Ranchi is Jharkhand's capital."),
        ("Dehradun is the capital of which state?", "Uttarakhand", "Himachal Pradesh", "Haryana", "Punjab", "Dehradun is Uttarakhand's winter capital / main capital."),
        ("Shimla is the capital of which state?", "Himachal Pradesh", "Uttarakhand", "J&K", "Sikkim", "Shimla is Himachal's capital."),
        ("Chandigarh is the capital of Punjab and?", "Haryana", "Himachal Pradesh", "Delhi", "Rajasthan", "Chandigarh is a shared capital and a UT."),
        ("Panaji is the capital of which state?", "Goa", "Maharashtra", "Karnataka", "Kerala", "Panaji (Panjim) is Goa's capital."),
        ("Thiruvananthapuram is the capital of which state?", "Kerala", "Tamil Nadu", "Karnataka", "Goa", "Thiruvananthapuram is Kerala's capital."),
        ("Amaravati / Hyderabad served as capital arrangements after 2014 for?", "Andhra Pradesh", "Telangana only", "Odisha", "Karnataka", "AP used Hyderabad as joint capital for a period after bifurcation."),
    ]
    for i, row in enumerate(nexus_rows[:200]):
        prompt, correct, w1, w2, w3, expl = row
        qs.append(item("nexus", i + 1, prompt, correct, [w1, w2, w3], expl))
    return qs


def pad(cat: str, qs: list[dict]) -> list[dict]:
    seen: set[str] = set()
    uniq: list[dict] = []
    for q in qs:
        if q["prompt"] in seen:
            continue
        seen.add(q["prompt"])
        uniq.append(q)
    if len(uniq) < 200:
        raise SystemExit(f"{cat} has only {len(uniq)} unique questions")
    out = []
    for i, q in enumerate(uniq[:200], 1):
        nq = dict(q)
        nq["id"] = f"{cat}_{i:03d}"
        nq["difficulty"] = diff_for(i)
        nq["prizeLevel"] = (i - 1) % 15
        out.append(nq)
    return out


def main() -> None:
    banks = {
        "gk": pad("gk", build_gk()),
        "sci": pad("sci", build_sci()),
        "his": pad("his", build_his()),
        "spo": pad("spo", build_spo()),
        "mov": pad("mov", build_mov()),
        "tech": pad("tech", build_tech()),
        "fusion": pad("fusion", build_fusion()),
        "nexus": pad("nexus", build_nexus()),
    }
    all_q = []
    for cat, _ in CATEGORIES:
        qs = banks[cat]
        prompts = [q["prompt"] for q in qs]
        if len(set(prompts)) != 200:
            raise SystemExit(f"duplicate prompts in {cat}: {200 - len(set(prompts))}")
        all_q.extend(qs)
        print(f"{cat}: {len(qs)}")
    OUT.write_text(json.dumps(all_q, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"wrote {len(all_q)} questions to {OUT}")


if __name__ == "__main__":
    main()
