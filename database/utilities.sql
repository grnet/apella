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
select u.*, rl.*, p.*, pd.*, r.* as rank, s.*, fs.*, i.*, sch.*, d.*
from professordomestic pd
join professor p on p.id = pd.id
join roles rl on rl.id = p.id
join users u on u.id = rl.user_id
left join rank r on r.id = pd.rank_id
left join subject s on s.id = pd.subject_id
left join subject fs on fs.id = pd.fekSubject_id
left join department d on d.id = pd.department_id
left join school sch on sch.id = d.school_id
left join institution i on i.id = sch.institution_id

select u.*, rl.*, p.*, pf.*, r.* as rank, s.*, c.*
from professorforeign pf
join professor p on p.id = pf.id
join roles rl on rl.id = p.id
join users u on u.id = rl.user_id
left join rank r on r.id = pf.rank_id
left join subject s on s.id = pf.subject_id
left join country c on c.code = pf.country_code

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

select type, professor_id, count(pfile.id) from professorfile pfile 
join fileheader fh on fh.id = pfile.id and fh.deleted=false and fh.type='PROFILE'
group by type, professor_id
having count(pfile.id) > 1

select * from professorfile pfile 
join fileheader fh on fh.id = pfile.id and fh.deleted=false and fh.type='PROFILE'
where professor_id in (54082)

select * from filebody fb where header_id in (
	select pfile.id from professorfile pfile 
	join fileheader fh on fh.id = pfile.id and fh.deleted=false and fh.type='PROFILE'
	where professor_id in (54082)
)
update fileheader set currentbody_id = null where id = 54085;
delete from filebody where header_id = 54085;
delete from professorfile where id = 54085;
delete from candidatefile where id = 54085;
delete from fileheader where id = 54085;

update fileheader set currentbody_id = null where id in (select header_id from filebody where originalfilename is null and id=currentbody_id);
delete from filebody where originalfilename is null;
delete from professorfile where id in (select id from fileheader where  currentbody_id is null);
delete from candidatefile where id in (select id from fileheader where  currentbody_id is null);
delete from fileheader where currentbody_id is null;

select * from fileheader where id not in ( (select id from professorfile) union (select id from candidatefile) );