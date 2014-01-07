/* DELETE PROFESSOR DOMESTIC */
delete from professordomestic where id in (
	select id from roles r where r.user_id = 1703
);
delete from professor where id in (
	select id from roles r where r.user_id = 1703
);
delete from candidate where id in (
	select id from roles r where r.user_id = 1703
);
delete from roles where user_id = 1703;
delete from users where id = 1703;

/* DELETE CANDIDATE */
delete from candidate where id in (
	select id from roles r where r.user_id = 3
);
delete from roles where user_id = 3;
delete from users where id = 3;

﻿/* CLEAN UP PROFESSOR DOMESTIC */
delete from professordomestic where id in (
	select id from roles r where r.user_id in (
		select id from users where users.contactinfo_email in (
			select email from professordomesticdata
		)
	)
);
delete from professor where id in (
	select id from roles r where r.user_id in (
		select id from users where users.contactinfo_email in (
			select email from professordomesticdata
		)
	)
);
delete from candidate where id in (
	select id from roles r where r.user_id in (
		select id from users where users.contactinfo_email in (
			select email from professordomesticdata
		)
	)
);
delete from roles where user_id in (
	select id from users where users.contactinfo_email in (
		select email from professordomesticdata
	)
);
delete from users where id in (
	select id from users where users.contactinfo_email in (
		select email from professordomesticdata
	)
);