DROP VIEW IF EXISTS "SonstigesEinfach";

CREATE VIEW "SonstigesEinfach" AS

SELECT

    "Versuchsbedingungen_Sonstiges"."Versuchsbedingungen" AS "Versuchsbedingung",
    "SonstigeParameter"."ID" AS "SonstigesID",

    "SonstigeParameter"."Beschreibung",
    "Einheiten"."Einheit",
    "DoubleKennzahlen"."Wert"

FROM "Versuchsbedingungen_Sonstiges"

LEFT JOIN "Einheiten"
ON "Versuchsbedingungen_Sonstiges"."Einheit"="Einheiten"."ID"

JOIN "SonstigeParameter"
ON "Versuchsbedingungen_Sonstiges"."SonstigeParameter"="SonstigeParameter"."ID"

LEFT JOIN "DoubleKennzahlen"
ON "Versuchsbedingungen_Sonstiges"."Wert"="DoubleKennzahlen"."ID";

GRANT SELECT ON TABLE "SonstigesEinfach" TO "PUBLIC";