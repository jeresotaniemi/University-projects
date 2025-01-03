import random
import time
import haravasto

tila = {
    "kentta": [],
    "tyhjakentta": [],
    "vapaat_ruudut": [],
    "miinat": [],
    "liput": [],
    "aika": [],
    "lopputulos": None
    }

def alkuvalikko():
    """
    Kysyy pelaajalta kentän koon sekä miinojen lukumäärän
    """
    print()
    print("Tervetuloa pelaamaan Miinaharavaa!")
    print("Valitse kentän leveys ja pituus sekä miinojen lukumäärä. Anna arvot kokonaislukuna.")
    print()
    while True:
        try:
            leveys = int(input("Syötä kentän leveys: "))
            pituus = int(input("Syötä kentän pituus: "))
            miinat_lkm = int(input("Syötä miinojen lukumäärä: "))
            tila["miinat"] = miinat_lkm
            if leveys < 1 or pituus < 1:
                print("Valitsit liian pienen kentän!")
            elif leveys * pituus < miinat_lkm:
                print("Valitsit enemmän miinoja kuin ruutuja!")
            elif miinat_lkm < 1:
                print("Miinoja ei voi olla olematta!")
            else:
                return leveys, pituus, miinat_lkm
        except ValueError:
            print("Et antanut kokonaislukua!")

def luo_kentta():
    """
    Luo kentän pelaajan antamien asetusten mukaisesti
    """
    leveys, pituus, miinat_lkm = alkuvalikko()

    kentta = []
    for rivi in range(pituus):
        kentta.append([])
        for sarake in range(leveys):
            kentta[-1].append(" ")
    tila["kentta"] = kentta

    vapaat_ruudut = []#Tässä luodaan lista koordinaattipareista miinoitusta varten.
    for x in range(leveys):
        for y in range(pituus):
            vapaat_ruudut.append((x, y))
    tila["vapaat_ruudut"] = vapaat_ruudut

    tyhjakentta = []
    for rivi in range(pituus):
        tyhjakentta.append([])
        for sarake in range(leveys):
            tyhjakentta[-1].append(" ")
    tila["tyhjakentta"] = tyhjakentta

    miinoita(kentta, vapaat_ruudut, miinat_lkm)
    numeroi_ruudut(kentta)

def miinoita(kentta, vapaat_ruudut, miinat_lkm):
    """
    Asettaa kentälle N kpl miinoja satunnaisiin paikkoihin.
    """
    miinat = []
    n = 1
    while n <= miinat_lkm:
        n += 1
        koordinaattipari = random.choice(vapaat_ruudut)
        x_koordinaatti = koordinaattipari[0]
        y_koordinaatti = koordinaattipari[1]
        vapaat_ruudut.remove(koordinaattipari)
        kentta[y_koordinaatti][x_koordinaatti] = "x"
        miinat.append((x_koordinaatti, y_koordinaatti))
    tila["miinat"] = miinat
    tila["kentta"] = kentta

def numeroi_ruudut(kentta):
    """
    Muuttaa ruutujen arvot miinojen lukumäärän mukaan
    """
    for y, rivi in enumerate(tila["kentta"]):
        for x, ruutu in enumerate(rivi):
            if ruutu != "x":
                arvot = laske_ruudut(x, y)
                if arvot > 0:
                    kentta[y][x] = str(arvot)
                else:
                    kentta[y][x] = "0"
    tila["kentta"] = kentta

def laske_ruudut(x, y):
    """
    Laskee yhden ruudun ympärillä olevat miinat
    """
    ruudut = 0
    for rivi in tila["kentta"][max(0, y - 1): min(y + 2, len(tila["kentta"]))]:
        for merkki in rivi[max(0, x - 1) : min(x + 2, len(rivi))]:
            if merkki == "x":
                ruudut += 1
    return ruudut

def avaa_ruutu(x, y):
    """
    Avaa tyhjällä kentällä olevan ruudun
    """
    tarkista_havio(x, y)
    if tila["tyhjakentta"][y][x] == " ":
        tila["tyhjakentta"][y][x] = tila["kentta"][y][x]
        if tila["kentta"][y][x] == "0":
            tulvataytto(x, y)
    piirra_kentta()

def laita_lippu(x, y):
    """
    Laittaa lipun tyhjälle kentälle tai poistaa sen
    """
    if tila["tyhjakentta"][y][x] == " ":
        tila["tyhjakentta"][y][x] = "f"
        tila["liput"].append((x, y))
        tarkista_voitto(x, y)
    elif tila["tyhjakentta"][y][x] == "f":
        tila["tyhjakentta"][y][x] = " "
        tila["liput"].remove((x, y))
    piirra_kentta()

def tarkista_voitto(x, y):
    """
    Tarkistaa, että tyhjällä kentällä olevat liput ovat samassa kohdassa kuin miinakentän miinat.
    Jos liput ovat oikeilla paikoilla, pelaaja voittaa pelin.
    """
    if set(tila["liput"]) == set(tila["miinat"]):
        print("Voitit pelin")
        print("Pelin tiedot on tallennettu 'pelitiedosto.txt' tekstitiedostoon.")
        tila["lopputulos"] = True
        tallenna_pelitiedot()
        piirra_kentta()

def tarkista_havio(x, y):
    """
    Tarkistaa, osuuko pelaajan klikkaus miinakentän miinaan.
    Jos osuu, pelaaja häviää.
    """
    if tila["kentta"][y][x] == "x" and tila["tyhjakentta"][y][x] == " ":
        print("Hävisit pelin")
        print("Pelin tiedot on tallennettu 'pelitiedosto.txt' tekstitiedostoon.")
        tila["lopputulos"] = False
        tallenna_pelitiedot()
        tila["tyhjakentta"] = tila["kentta"]
        piirra_kentta()
    elif tila["tyhjakentta"][y][x] == "f":
        print("Poista ensin lippu ennen kun voit avata ruudun.")

def aloita_ajanotto():
    """
    Aloittaa ajanoton
    """
    tila["aika"] = time.time()

def lopeta_ajanotto():
    """
    Lopettaa ajanoton, kun peli on loppunut
    """
    lopetusaika = time.time()
    if tila["lopputulos"] is True or tila["lopputulos"] is False:
        kesto = lopetusaika - tila["aika"]
    return kesto

def piirra_kentta():
    """
    Käsittelijäfunktio, joka piirtää kaksiulotteisena listana kuvatun miinakentän
    ruudut näkyviin peli-ikkunaan. Funktiota kutsutaan aina kun pelimoottori pyytää
    ruudun näkymän päivitystä.
    """
    haravasto.tyhjaa_ikkuna()
    haravasto.piirra_tausta()
    haravasto.aloita_ruutujen_piirto()
    for y, rivi in enumerate(tila["tyhjakentta"]):
        for x, ruutu in enumerate(rivi):
            haravasto.lisaa_piirrettava_ruutu(ruutu, x * 40, y * 40)
    haravasto.piirra_ruudut()

def kasittele_hiiri(x_sijainti, y_sijainti, painike, muokkausnapit):
    """
    Tätä funktiota kutsutaan kun käyttäjä klikkaa sovellusikkunaa hiirellä.
    Tulostaa hiiren sijainnin sekä painetun napin terminaaliin.
    """
    x_sijainti = int(x_sijainti / 40)
    y_sijainti = int(y_sijainti / 40)
    if painike == haravasto.HIIRI_VASEN:
        avaa_ruutu(x_sijainti, y_sijainti)
    elif painike == haravasto.HIIRI_OIKEA:
        laita_lippu(x_sijainti, y_sijainti)

def tulvataytto(x_koordinaatti, y_koordinaatti):
    """
    Merkitsee planeetalla olevat tuntemattomat alueet turvalliseksi siten, että
    täyttö aloitetaan annetusta x, y -pisteestä.
    """
    aloituspisteet = []
    aloituspisteet.append((x_koordinaatti, y_koordinaatti))
    while aloituspisteet:
        koordinaattipari = aloituspisteet.pop()
        xt = koordinaattipari[0]
        yt = koordinaattipari[1]
        if tila["tyhjakentta"][yt][xt] == "0":
            for y, rivi in enumerate(tila["tyhjakentta"][max(0, yt - 1): min(yt + 2, len(tila["tyhjakentta"]))], start = max(0, yt - 1)):
                for x, ruutu in enumerate(rivi[max(0, xt - 1) : min(xt + 2, len(rivi))], start = max(0, xt - 1)):
                    if ruutu == " ":
                        aloituspisteet.append((x, y))
                        tila["tyhjakentta"][y][x] = tila["kentta"][y][x]

def tallenna_pelitiedot():
    """
    Luo tiedoston johon pelitiedot tallennetaan ja lisää tiedot sinne.
    """
    if tila["lopputulos"] is True:
        lopputulos = "Voitto"
    else:
        lopputulos = "Häviö"
    with open("pelitiedosto.txt", "w") as kohde:
        kohde.write("Pelin ajankohta: {}\nPeliin kulunut aika: {:.2f} sekuntia\nLopputulos: {}\nKentän koko: {} x {}\nMiinojen lukumäärä: {}\n".format(
        time.strftime("%d.%m.%Y %H:%M:%S", time.localtime()),
        lopeta_ajanotto(),
        lopputulos,
        len(tila["kentta"][0]),
        len(tila["kentta"]),
        len(tila["miinat"])
        ))

def main():
    """
    Lataa pelin grafiikat, luo peli-ikkunan ja asettaa siihen piirtokäsittelijän.
    """
    luo_kentta()
    haravasto.lataa_kuvat("C:\\Users\\jeres\\Minesweeper\\spritet.zip\\spritet")
    haravasto.luo_ikkuna(len(tila["kentta"][0]) * 40, len(tila["kentta"]) * 40)
    haravasto.aseta_piirto_kasittelija(piirra_kentta)
    haravasto.aseta_hiiri_kasittelija(kasittele_hiiri)
    aloita_ajanotto()
    haravasto.aloita()


if __name__ == "__main__":
    main()