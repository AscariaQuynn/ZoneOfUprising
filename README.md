ZoneOfUprising
==============

Space Shooter

my first project, readme maybe later. no one will read it




TODO:
-----

app state manager pujde nahradit mym state managerem v application a zkusit v base app state predelat initialize a poustet nacasteny initialize(ZoUAppStateManager, ZoneOfUprising)


do particle controlleru implementovat box source a sphere source
setCcdMotionThreshold()	The amount of motion in 1 physics tick to trigger the continuous motion detection in moving objects that push one another. Rarely used, but necessary if your moving objects get stuck or roll through one another.

zkusit esi to de i s gimpact
https://code.google.com/p/jmonkeyengine/source/browse/trunk/engine/src/test/jme3test/bullet/TestAttachGhostObject.java

Promyslet: bulletproof app state managing
- do baseappstate implementovat List se zavislostmi na jinych app state a pri initialize a cleanupu v base state kontrolovat, jestli je mozne state pridat a odebrat
- implementovat abstract public void checkStatesInitialize a checkStatesCleanup ktere kontroluji jestli je mozne state pridat ci odebrat a implementovat eventy, ktere pri pridani/odebrani app state informuji ostatni app state a ty rozhodnou, jestli bez ostatnich app state nemuzou byt a muzou vyhodit co nejvic odpovidajci exceptionu
- zvazit jestli jde appstates do tech kontrolujcich eventu pridavat automaticky a uzivatel by jen implementovat abstract check a event metody
- projektil bude mit base speed a delo speed modifier, nebo naopak? pripadne obojimu oboji? ze by se zakladni rychlosti krizem vynasobily modifierama a pak secetly ? bulletSpd * turretMod + turretSpd * bulletMod? nebo bez krize? ale proc potom modifier ? bych mohl napsat vynasobeny cisla rovnou... to krizeni asi bude fajn, promyslet ho...

Phase I:
--------
- opozdene spousteni predelat z scheduled executoru na timer
- pri pridavani a odebirani z targetables pustit event added removed a zbrane muzou odposlouchavat targetables kvuli unlock target a nemusej resit target parent node, ani entity unloaded
- pro json pouzit http://examples.javacodegeeks.com/core-java/json/java-json-parser-example/
- jednotlive entityitemy budou mit hmotnost a lod bude mit max nosnost, scitat to do celkove hmotnosti a tu bude mit fyzika lodi
- properties helper dostane properties z item a z entityitem a bude prohledavat nejdriv entityitem.properties a pak item.properties jako stock fallback, takze nebude nutne zdvojovat stejne properties v databazi
- user bude mit svuj inventar kam bude kupovat veci z obchodu a lodicka bude mit taky inventar kam bude sbirat veci z vesmiru
- na lodicku pujdou narvat veci z jejiho inventare, ci user inventare abych se nemusel central serveru dotazovat na itemy... bude stacit hracuv a lodni inventar
- kompenzatory prejmenovat na asistenty a pak tam pujdou pridavat ruzny blbuvzdorny asistenti jako treba varovani pred kolizi, popremyslet jestli umoznit asistentum prebrat kontrolu nad lodi a tudiz zahazovat user input kdyz budou chtit
- autopilot bude asi jako asistent ktery prebere uplnou kontrolu nad lodi a AI bude vyuzivat autopilota, takze neni nutne kontrolovat aiControl enabled
- autopilot bude podporovat steering behaviours, nikoli AI
- thrustry predelat do db a jako particle controller
- aktivace AI prevezme kontrolu nad lodi uplne, takze user input to bude ignorovat a thrustery se budou aktivovat silama, pri aktivni ai nebudou reagovat ani motory
- udelat vysledny damage z kolize souctem 1-3 nezavislych kolizi, behem jednoho delta timeoutu aby se nestavalo, ze silyn naraz da 50 dmg a viditelne slabsi 500dmg
- star dust efekt nedelat kolem lodi, ale kolem kamery
- implement vector3d (a mozna quaterniond i kdyz to asi nebude nutny) pro double kalkulace a zkusit to navliknout na kalkulaci rotacniho odporu v rotacnim kompenzatoru
- hodnoty pro total mass, entity item masses, atd. budou double, ne float v entity a entity profile
- movement will be only on action listener, analog messes up acceleration when fps is low
- pridat metody set float get float atd pro ty json parametry, nebo argument string pro nastavovani druhotnych veci
- entity profile a item bude moct brat vlastnosti itemu stylem entityProfile.getColorRGBA(name, default) item.getFloat(name, default)
- pokud bude vypnuty rotation control nebo movement control, vyse uvedene metody budou vracet vzdy 1f, protoze budeme predpokladat, ze se lod vzdy kouka ci leti spravne
- arrival pouziva engines control na ruseni bocniho pohybu a enginescontrol's rotacni dot na urceni jestli zazehnout zadni trysky bude nastavovat motorum required rotation a zazehavat trysky motoru
- projektilu se predava collidables, projektilove collidables by melo byt schopno vyradit svou entitu z detekce kolizi. jelikoz ale nemuzu pole zduplikovat, protoze by tam zustaly znicene lode, tak pri prochazeni collidables a aplikovani collidewith, musim pridat podminku co odignoruje sama sebe
- collidables zvazit udelat jako pair entityprofile-entity a vubec vytvorit a zavest entityprofile-entity pairy navazany na entity name, pak to pujde lehce narvat do systemu a melo by z toho jit i urcovat, jestli je hrac naloadovany ale jeho entita je znicena, cili respawn
- synchronizovat entitu a vsechny jeji projektily v jedny msg, treba to zabrani trefovani sebe sama a cukani mezi projektilama a entitou
- refaktorovat user input control, aby se to chovalo spravne
- po user input control refaktorovat thrustery, aby se zapinaly pri AI a i pri user klikani

- ok - game server a central server projede vsechny mozny adresy jak se k nemu da pripjit a vsechny je vypise, ne jen jednu a ne jen game server
- ok - entitybuilder staci jeden v entity managerovi
- ok jako EntityItem.getProperty*(String name) - dodatecny nastaveni lode jako pole v jednom argumentu
- ok jako EntityItem s typem gun, engine etc. - pridat do entity profilu zbrane jako pole
- ok - pridat maximalni povolenou kinetickou energii, pri ktere nebude lodicka dostavat damage, zkusit to nastavit na nizkou hodnotu a poskodit lodicku pomoci akcelerace
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
---------
- zavest tridy lodi a vhodnost entityitemu pro dane tridy lodi, aby heavy motor nesel do stihacky, ale light motor muze do krizniku, takze asi jen urcovat, do az jake tridy je entityitem vhodny
- rozlisovat tridy lodi dle min a max tonnage?
- vhodnost entityitemu pro tridy lodi bude v jednom sloupecku jako json string a filtrovani bude az na strane herniho klienta
- setAIControl z engines a thrustru predelat do artificialintelligence.class a z control kontrolovat AI jestli ma control, mozna ot udelat jako event
- ve smeru linear velocity posilat raycast do pevnyho bodu a pocitat k nemu drahu a dobrzdeni, jestli to jde ubrzdit a kdy presne
- spocitat angular kinetic force a davat i damage z prudky rotace
- pri detekci kolize si zapamatovavat vic nez jen jednu kolizi, aby hraci nemohli nekoho prizdit a znicit ho "turbo kolizema"
  omezit treba na 3-5 kolizi, pri novy kolizi vyhodit nejstarsi kolizi a novou hodit na konec, linked list bude fajn
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
----------
- lodicka bude mit energii v podobe poctu bodu a kazdy zapnuty system bude z teto energie cepat nejake body, kdyz bude zbyvajcich bodu (treba reprezentovanych jako procent) malo, funkce nepujde zapnout potom sync message by mela byt schopna pojmout nastaveni ze vice control najednou
- moznost vybrat herni mod [deathmatch, team deathmatch]
- settings menu s nastavitelnymi volbami
- engines control by mela mit metody, ktere vrati maximalni mozny thrust a torque (nejak vykalkulovat z pid controlleru, treba vectorx cross vectory, pravy uhel, aby to udelalo max error, nebo jen rict pid controlleru at vrati max error)


Misc:
-----
- hud
- - energie pro systemy
- - - stity: malo, stredne, hodne - rychlost nabijeni
- - - zbrane: kanony na munici - pomalu, stredne, rychle - kadence
- - - lasery - -||- - sila utoku
- - - rakety - nema vliv, budou videt pod lodickou, kazda lodicka bude mit missile sloty
- - 3d radar s blizkyma entitama
- - oznaceni zamireneho cile sestiuhelnikem
