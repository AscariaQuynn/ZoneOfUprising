ZoneOfUprising
==============

Space Shooter

my first project, readme maybe later. no one will read it




TODO:

- vpravo dole v hudu 3d radar s cilema
- vlevo dole v hudu nejaka pristrojova deska a na ni toggle buttony pro zbranove mody
a ikonky spotlight a pointlight, taky asi jako toggle buttony
ok - lodicka bude mit maly zapinatelny point light kolem sebe, aby byla schopna
osvetlit nejblizsi okoli, napriklad v jeskyni v asteroidu
ok - lodicka bude mit spotlight na osvetlovani predku
- lodicka bude mit energii v podobe poctu bodu a kazdy zapnuty system bude z teto energie
cepat nejake body, kdyz bude zbyvajcich bodu (treba reprezentovanych jako procent) malo,
funkce nepujde zapnout
- zvazit jestli neodebrat nejake funkce z shipcontrol a nepremistit je do samostatych kontrol
potom sync message by mela byt schopna pojmout nastaveni ze vice control najednou
- strileni zkusit vykoumat jako to navrhuje normen hansen, ale nechapu porad jak to mysli
http://hub.jmonkeyengine.org/forum/topic/predict-target-location-at-projectiles-arrival/
co NUTNEHO je potreba k dokonceni hry
- settings menu s nastavitelnymi volbami
- join server
- - moznost vybrat mapu, pro zacatek alespon 2
- - moznost vybrat herni mod [deathmatch, team deathmatch]
- - prozkoumat moznost spusteni lokalniho serveru ze hry, idealne aby ji klient mohl spustit na lokale a samo mu ot rekne kdy je spusteno
- ingame
- - esc -> ingame menu
- - - ve vrsku okna sepsat ovladani
- - - pokracovat
- - - ukoncit mapu
- hud
- - hitpointy do kategorii: nabijeci shield, opravovatelny armor, struktura
- - energie pro systemy
- - - stity: malo, stredne, hodne - rychlost nabijeni
- - - zbrane: kanony na munici - pomalu, stredne, rychle - kadence
- - - lasery - -||- - sila utoku
- - - rakety - nema vliv, budou videt pod lodickou, kazda lodicka bude mit missile sloty
- - 3d radar s blizkyma entitama
- - oznaceni zamireneho cile sestiuhelnikem



- brzdeni bocniho pohybu ma nastarosti vylucne engines control
- otaceni za cilem ma nastarosti engines control, ktera vraci dot product z rotace (getRequiredRotationDot)
- rotation pid nema nastarosti brzdeni rotace, to se bude vypocitavat uvnitr engines control
- engines control bude mit metody getLinearVelocityDot a getRotationDot, ktere bude nepretrzite nastavovat
v zavislosti na tom, jak moc je smer lodi mimo od pozadovaneho smeru (lod driftuje),
nebo je jeji cumak mimo od pozadovane rotace (lod kouka jinam nez chceme)
- pokud bude vypnuty rotation control nebo movement control, vyse uvedene metody budou vracet
vzdy 1f, protoze budeme predpokladat, ze se lod vzdy kouka ci leti spravne
- arrival pouziva engines control na ruseni bocniho pohybu a enginescontrol's
rotacni dot na urceni jestli zazehnout zadni trysky
bude nastavovat motorum required rotation a zazehavat trysky motoru
- aimcontrol pouziva engines control na zamireni pozadovane rotace lodi
tzn bude jen nastavovat required rotation motorum
- nejak vymyslet, aby bylo mozno zadavat motorum i tah, aby nebyl vzdy 100%
- movement compensator nesmi prekrocit hodnotu linearni hybnosti
- rotation compensator nesmi prekrocit hodnotu angularni hybnosti
- dodatecny nastaveni lode jako pole v jednom argumentu
- doresit brzdeni bocniho pohybu
- rotacni pohyb nastavit nejake brzdici minimum aby se brzdici sila nezmensovala do nekonecna
- sjednotit synchronizaci projektilu a lode do jednoho pole
- pridat do entity profilu zbrane jako pole
- pridat metody set float get float atd pro ty json parametry
- - nebo argument string pro nastavovani druhotnych veci



- brzdeni bocniho pohybu ma nastarosti vylucne engines control
- otaceni za cilem ma nastarosti engines control, ktera vraci dot product z rotace (getRequiredRotationDot)
- rotation pid nema nastarosti brzdeni rotace, to se bude vypocitavat uvnitr engines control
engines control bude mit metody getLinearVelocityDot a getRotationDot, ktere bude nepretrzite nastavovat
v zavislosti na tom, jak moc je smer lodi mimo od pozadovaneho smeru (lod driftuje),
nebo je jeji cumak mimo od pozadovane rotace (lod kouka jinam nez chceme)
- pokud bude vypnuty rotation control nebo movement control, vyse uvedene metody budou vracet
vzdy 1f, protoze budeme predpokladat, ze se lod vzdy kouka ci leti spravne
- arrival pouziva engines control na ruseni bocniho pohybu a enginescontrol's
rotacni dot na urceni jestli zazehnout zadni trysky
bude nastavovat motorum required rotation a zazehavat trysky motoru
- aimcontrol pouziva engines control na zamireni pozadovane rotace lodi
tzn bude jen nastavovat required rotation motorum
- nejak vymyslet, aby bylo mozno zadavat motorum i tah, aby nebyl vzdy 100%
- pri zmacknuti dvou sipek do opacnych stran musi byt vysledek 0 a ne +(-)1
- svet se bude zacitat stejne jako entita, pomoci requestWorld (sjednotit chovani s entitou),
po obdrzeni server profile si server zazada o svet a bude mu vracen selected svet
- v severovy konzoly udelat prikaz na vypsani dostupnych svetu a prikaz na nacteni sveta
- nejdriv asi reloadovani sveta kopne vsechny entity, ale mohlo by jim to zobrazit loading
a nacit rovnou jiny svet taky
ok - kdyz mam malo hp, pustit sirenu, jen na svy vlastni lodi
- linear force bude poustet jen predni a zadni trysky, bocni jen angular force jak je to ted
- - zajistit aby po pusteni klaves po vypnuti perma trysek se nespustily tmp trysky
- - pri ovladani rotace mysi nebo sipkama, udelat taky u hrace perma rotacni trysky
- - treba pri vypnuti perma trysek bude 0.5sec timeout ingnorovani tmp trysek
- engines control by mela mit metody, ktere vrati maximalni mozny thrust a torque (nejak
vykalkulovat z pid controlleru, treba vectorx cross vectory, pravy uhel, aby to udelalo max error,
nebo jen rict pid controlleru at vrati max error)
a podle toho bude thrusters control adjustovat velocity trysek
a treba i pocet particles od 50% (0) - 100% (max thrust)
- projektilu se predava collidables, projektilove collidables by melo byt schopno vyradit
svou entitu z detekce kolizi. jelikoz ale nemuzu pole zduplikovat, protoze by tam zustaly
znicene lode, tak pri prochazeni collidables a aplikoavni collidewith, musim pridat podminku
co odignoruje sama sebe
- collidables zvazit udelat jako pair entityprofile-entity a vubec vytvorit a zavest
entityprofile-entity pairy navazany na entity name, pak to pujde lehce narvat do systemu
a melo by z toho jit i urcovat, jestli je hrac nalodovany ale jeho entita je znicena,
cili respawn
- entitybuilder staci jeden v entity managerovi
- class entity co bude obsahovat spatial a entityprofile, zvazit jak udelat entityupdater
- class entity bude umet respawnovat
- restart world ve world managerovi, zachova entity s entityprofile ale uz ne spatial
- zbavit se getrotationcolumn a nahradit to za q.mult(vector_z...)
- entity profile a item bude moct brat vlastnosti itemu stylem
entityProfile.getColorRGBA(name, default) item.getFloat(name, default)
