ZoneOfUprising
==============

Space Shooter

my first project, readme maybe later. no one will read it




TODO:

Phase I:
- movement will be only on action listener, analog messes up acceleration when fps is low
- rotation control drag udelat asi dynamicky
- entitybuilder staci jeden v entity managerovi
- dodatecny nastaveni lode jako pole v jednom argumentu
- pridat do entity profilu zbrane jako pole
- pridat metody set float get float atd pro ty json parametry, nebo argument string pro nastavovani druhotnych veci
- entity profile a item bude moct brat vlastnosti itemu stylem entityProfile.getColorRGBA(name, default) item.getFloat(name, default)
- pokud bude vypnuty rotation control nebo movement control, vyse uvedene metody budou vracet vzdy 1f, protoze budeme predpokladat, ze se lod vzdy kouka ci leti spravne
- arrival pouziva engines control na ruseni bocniho pohybu a enginescontrol's rotacni dot na urceni jestli zazehnout zadni trysky bude nastavovat motorum required rotation a zazehavat trysky motoru
- projektilu se predava collidables, projektilove collidables by melo byt schopno vyradit svou entitu z detekce kolizi. jelikoz ale nemuzu pole zduplikovat, protoze by tam zustaly znicene lode, tak pri prochazeni collidables a aplikovani collidewith, musim pridat podminku co odignoruje sama sebe
- collidables zvazit udelat jako pair entityprofile-entity a vubec vytvorit a zavest entityprofile-entity pairy navazany na entity name, pak to pujde lehce narvat do systemu a melo by z toho jit i urcovat, jestli je hrac naloadovany ale jeho entita je znicena, cili respawn
- synchronizovat entitu a vsechny jeji projektily v jedny msg, treba to zabrani trefovani sebe sama a cukani mezi projektilama a entitou
- refaktorovat user input control, aby se to chovalo spravne
- po user input control refaktorovat thrustery, aby se zapinaly pri AI a i pri user klikani

- ok - pri zmacknuti dvou sipek do opacnych stran musi byt vysledek 0 a ne +/-1
- ok - po reloadu sveta obcas nefunguje detekce kolizi
- ok - revisit - engines control bude mit metody getLinearVelocityDot a getRotationDot, ktere bude nepretrzite nastavovat v zavislosti na tom, jak moc je smer lodi mimo od pozadovaneho smeru (lod driftuje), nebo je jeji cumak mimo od pozadovane rotace (lod kouka jinam nez chceme)
- ok - zbavit se getrotationcolumn a nahradit to za q.mult(vector_z...)
- x ok - rotacni pohyb nastavit nejake brzdici minimum aby se brzdici sila nezmensovala do nekonecna
- ok - doresit brzdeni bocniho pohybu
- ok - lodicka bude mit maly zapinatelny point light kolem sebe, aby byla schopna osvetlit nejblizsi okoli, napriklad v jeskyni v asteroidu
- ok - lodicka bude mit spotlight na osvetlovani predku
- ok - zvazit jestli neodebrat nejake funkce z shipcontrol a nepremistit je do samostatych kontrol
- ok - strileni zkusit vykoumat jako to navrhuje normen hansen, ale nechapu porad jak to mysli
     http://hub.jmonkeyengine.org/forum/topic/predict-target-location-at-projectiles-arrival/
- ok - brzdeni bocniho pohybu ma nastarosti vylucne engines control
- ok - otaceni za cilem ma nastarosti engines control, ktera vraci dot product z rotace (getRequiredRotationDot)
- ok - rotation pid nema nastarosti brzdeni rotace, to se bude vypocitavat uvnitr engines control
- ok - aimcontrol pouziva engines control na zamireni pozadovane rotace lodi tzn bude jen nastavovat required rotation motorum
- ok - movement compensator nesmi prekrocit hodnotu linearni hybnosti
- ok - rotation compensator nesmi prekrocit hodnotu angularni hybnosti
- ok - svet se bude nacitat stejne jako entita, pomoci requestWorld (sjednotit chovani s entitou), po obdrzeni server profile si server zazada o svet a bude mu vracen selected svet
- ok - v severovy konzoly udelat prikaz na vypsani dostupnych svetu a prikaz na nacteni sveta
- ok nekope - nejdriv asi reloadovani sveta kopne vsechny entity, ale mohlo by jim to zobrazit loading a nacit rovnou jiny svet taky
- ok - kdyz mam malo hp, pustit sirenu, jen na svy vlastni lodi
- ok - class entity co bude obsahovat spatial a entityprofile, zvazit jak udelat entityupdater
- x ok - class entity bude umet respawnovat
- ok - restart world ve world managerovi, zachova entity s entityprofile ale uz ne spatial


Phase II:
- sjednotit synchronizaci projektilu a lode do jednoho pole
- hitpointy do kategorii: nabijeci shield, opravovatelny armor, struktura
- vpravo dole v hudu 3d radar s cilema
- vlevo dole v hudu nejaka pristrojova deska a na ni toggle buttony pro zbranove mody a ikonky spotlight a pointlight, taky asi jako toggle buttony
- nejak vymyslet, aby bylo mozno zadavat motorum i tah, aby nebyl vzdy 100%
- linear force bude poustet jen predni a zadni trysky, bocni jen angular force jak je to ted
- - zajistit aby po pusteni klaves po vypnuti perma trysek se nespustily tmp trysky
- - pri ovladani rotace mysi nebo sipkama, udelat taky u hrace perma rotacni trysky
- - treba pri vypnuti perma trysek bude 0.5sec timeout ingnorovani tmp trysek a podle toho bude thrusters control adjustovat velocity trysek a treba i pocet particles od 50% (0) - 100% (max thrust)


Phase III:
- lodicka bude mit energii v podobe poctu bodu a kazdy zapnuty system bude z teto energie cepat nejake body, kdyz bude zbyvajcich bodu (treba reprezentovanych jako procent) malo, funkce nepujde zapnout potom sync message by mela byt schopna pojmout nastaveni ze vice control najednou
- moznost vybrat herni mod [deathmatch, team deathmatch]
- settings menu s nastavitelnymi volbami
- engines control by mela mit metody, ktere vrati maximalni mozny thrust a torque (nejak vykalkulovat z pid controlleru, treba vectorx cross vectory, pravy uhel, aby to udelalo max error, nebo jen rict pid controlleru at vrati max error)


Misc:
- hud
- - energie pro systemy
- - - stity: malo, stredne, hodne - rychlost nabijeni
- - - zbrane: kanony na munici - pomalu, stredne, rychle - kadence
- - - lasery - -||- - sila utoku
- - - rakety - nema vliv, budou videt pod lodickou, kazda lodicka bude mit missile sloty
- - 3d radar s blizkyma entitama
- - oznaceni zamireneho cile sestiuhelnikem

