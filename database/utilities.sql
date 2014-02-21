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

/* Export Professors */
select u.*, r.*, p.*, pd.*, fs.*, s.*, ra.*,
(select pfile.id from professorfile pfile join fileheader fh on fh.id = pfile.id and fh.deleted=false and fh.type='FEK' where pfile.professor_id = pd.id ) as fekFile,
(select pfile.id from professorfile pfile join fileheader fh on fh.id = pfile.id and fh.deleted=false and fh.type='PROFILE' where pfile.professor_id = pd.id  ) as profileFile
from professordomestic pd
join professor p on p.id =pd.id
join roles r on r.id = pd.id
join users u on u.id = r.user_id
left join subject fs on fs.id = pd.feksubject_id
left join subject s on s.id = pd.subject_id
join rank ra on ra.id = pd.rank_id