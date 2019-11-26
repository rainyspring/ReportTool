/**
 * sql拼写过程中不能使用单行注视（2个中划线+一个空格 ）
 * 报表--焊工
 * */
select 
DISTINCT wr.id
,wr.code
,wr.name
,wr.sex
,wr.idCard
,wr.IDnumber
,wr.validTermOfCredentials
,wr.qualifiedProjectName
,wr.welderCredentialsFileNumber
,wr.welderCredentialsNumber
,wr.available
,wr.type
,wr.siteEntranceTime
,wr.siteExitTime
,wr.constructionTeam
,wr.note

,w.projectId
,w.projectItemId
,w.projectUnitId
,w.projectCode
,w.projectItemCode
,w.projectUnitCode
,w.projectName
,w.projectItemName
,w.projectUnitName

from 
(

SELECT 
wr.id
,wr.code
,wr.name
,wr.sex
,wr.idCard
,wr.IDnumber
,DATE_FORMAT(wr.validTermOfCredentials,'%Y%m%d') as validTermOfCredentials
,wr.welderCredentialsFileNumber
,wr.welderCredentialsNumber
,wr.available
,wr.type
,DATE_FORMAT(wr.siteEntranceTime,'%Y%m%d') as siteEntranceTime
,DATE_FORMAT(wr.siteExitTime,'%Y%m%d') as siteExitTime
,wr.constructionTeam
,wr.note
,wr.projectId
,GROUP_CONCAT(q.qualifiedProjectName SEPARATOR ',') as qualifiedProjectName
FROM pw_qualifiedproject q
INNER JOIN pw_welder wr 
ON q.welderId = wr.id
GROUP BY wr.id
)
wr /** 包含多个合格项目的全部焊工信息 */

inner JOIN (
 
select distinct  w.welderId
	,p.id as projectId, p.code as projectCode ,p.name as projectname
	,pi.id as projectItemId, pi.code as projectItemCode ,pi.name as projectItemname
	,pu.id as projectUnitId, pu.code as projectUnitCode ,pu.name as projectUnitname
from pw_weld w
INNER JOIN pw_project p ON w.projectId=p.id
INNER JOIN pw_projectitem pi ON w.projectItemId=pi.id
INNER JOIN pw_projectunit pu ON w.projectUnitId=pu.id
where w.welderId is not null
UNION 
select distinct  w.welderId_gm
	,p.id as projectId, p.code as projectCode ,p.name as projectname
	,pi.id as projectItemId, pi.code as projectItemCode ,pi.name as projectItemname
	,pu.id as projectUnitId, pu.code as projectUnitCode ,pu.name as projectUnitname
from pw_weld w
INNER JOIN pw_project p ON w.projectId=p.id
INNER JOIN pw_projectitem pi ON w.projectItemId=pi.id
INNER JOIN pw_projectunit pu ON w.projectUnitId=pu.id
where w.welderId_gm is not null

) w /* 涉及打底焊工和盖面焊工的焊缝的焊工信息*/

on w.welderId=wr.id
