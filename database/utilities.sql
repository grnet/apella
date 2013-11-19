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