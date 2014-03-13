CREATE OR REPLACE FUNCTION urlencode(in_str text, OUT _result text)
    STRICT IMMUTABLE AS $urlencode$
DECLARE
    _i      int4;
    _temp   varchar;
    _ascii  int4;
BEGIN
    _result = '';
    FOR _i IN 1 .. length(in_str) LOOP
        _temp := substr(in_str, _i, 1);
        IF _temp ~ '[0-9a-zA-Z:/@._?#-]+' THEN
            _result := _result || _temp;
        ELSE
            _ascii := ascii(_temp);
            IF _ascii > x'07ff'::int4 THEN
                RAISE EXCEPTION 'Won''t deal with 3 (or more) byte sequences.';
            END IF;
            IF _ascii <= x'07f'::int4 THEN
                _temp := '%'||to_hex(_ascii);
            ELSE
                _temp := '%'||to_hex((_ascii & x'03f'::int4)+x'80'::int4);
                _ascii := _ascii >> 6;
                _temp := '%'||to_hex((_ascii & x'01f'::int4)+x'c0'::int4)
                            ||_temp;
            END IF;
            _result := _result || upper(_temp);
        END IF;
    END LOOP;
    RETURN ;
END;
$urlencode$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION createAdministrator(username text, firstname text, lastname text, firstnamelatin text, lastnamelatin text, email text)
RETURNS void AS $$
DECLARE
userid bigint;
roleid bigint;
BEGIN
	
INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (nextval('hibernate_sequence'), null, 'USERNAME', '-', firstname,
	lastname, '-', firstnamelatin, 
	lastnamelatin, email, '6900000000', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', username, NULL, 1)
	RETURNING id INTO userid;
	
INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (nextval('hibernate_sequence'), 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, userid)
RETURNING id INTO roleid;

INSERT INTO administrator (id) 
VALUES (roleid);

END
$$ LANGUAGE plpgsql;