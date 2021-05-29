/**
 * sql拼写过程中不能使用单行注视（2个中划线+一个空格 ）
 *    报表数据源---焊缝
 */
select w.id AS weldId
/*焊缝号*/ 
,w.weldNumber 
/*材质*/  
,w.materialQuality
/*炉批号1*/  
,w.batchNo1 
/*炉批号2*/  
,w.batchNo2
/*公称直径*/  
,w.nominalDiameter
/*公称壁厚*/  
,w.nominalThickness 
/*规格*/  
,w.specifications 
/*理论寸经*/  
,w.theoreticalDiameter 
/*施工队伍*/  
,w.constructionTeam
/*焊接方法*/ 
,w.weldingMethod 
/*打底焊接材料*/  
,w.weldingMaterial
/*盖面施焊接材料 */ 
,w.weldingMaterial_gm 
/*焊接位置*/ 
,w.weldingPosition
/*是否为固定口*/ 
,(case when wp.type='G' then 'Y' else 'N' end) as weldingPosition_gd
/*是否为活动口*/ 
,(case when wp.type='Z' then 'Y' else 'N' end) as weldingPosition_hd
/*对接形式*/ 
,w.buttJointForm
/*焊接日期*/ 
,w.weldingDate 
/* 线能量*/
,w.lineCapacity 

,w.welderId 
,w.welderCode 
,w.welderName

,w.welderId_gm 
,w.welderCode_gm 
,w.welderName_gm 

/*预热温度*/ 
,w.preheatTemperature 
/*层间温度*/ 
,w.interpassTemperature 
/*后热温度*/ 
,w.postheatTemperature 
/*是否热处理*/ 
,w.heatTreating 
/*管外径*/ 
,w.externalDiameter AS externalDiameter_wl 
/*设计压力*/ 
,w.designPressure_wl AS designPressure_wl 
/*坡口形式*/ 
,w.grooveType AS grooveType 
/* 坡口形式*/
,w.ddWeldingDate AS ddWeldingDate 
/* 壁厚等级*/
,w.wallThicknessLevel_wl AS wallThicknessLevel_wl 


/*管道属性*/ 
,pl.*

,( case when nde.RT_status>0 then 1 else 0 end) as RT_status
,( case when nde.UT_status>0 then 1 else 0 end) as UT_status
,( case when nde.PT_status>0 then 1 else 0 end) as PT_status
,( case when nde.MT_status>0 then 1 else 0 end) as MT_status
,( case when nde.TOFD_status>0 then 1 else 0 end) as TOFD_status

, (case when ht.HTDate is not null then 1 else 0  end ) as HT_status

, (case when RT_status=1 then 'Y' else (case when RT_status=0 then 'N' else '-' end) end) as RT_statusName
, (case when UT_status=1 then 'Y' else (case when UT_status=0 then 'N' else '-' end) end) as UT_statusName
, (case when PT_status=1 then 'Y' else (case when PT_status=0 then 'N' else '-' end) end) as PT_statusName
, (case when MT_status=1 then 'Y' else (case when MT_status=0 then 'N' else '-' end) end) as MT_statusName
, (case when TOFD_status=1 then 'Y' else (case when TOFD_status=0 then 'N' else '-' end) end) as TOFD_statusName
, (case when ht.HTDate is not null then 'Y' else (case when ht.id is not null then 'N' else '-' end)  end ) as HT_statusName

,nde.ndetypeId
,nde.reportCode as ndeReportCode
,nde.ndeDate
,nde.ndeResult
,nde.picNumber
,nde.noNumber
,ht.HTReportCode
,date_format(ht.HTDate,'%Y%m%d') as HTDate
,ht.HTProcessExecution as htResult
 from pw_weld w

inner join 
(
/**
 * sql拼写过程中不能使用单行注视（2个中划线+一个空格 ）
 * 报表--管道
 * */
select 
pl.id AS pipelineId
/*数据范围*/ 
,pl.projectId 
,pl.projectItemId 
,pl.projectUnitId 
,p.code AS projectCode
,p.name AS projectName
,pi.code AS projectItemCode
,pi.name AS projectItemName
,pu.code AS projectUnitCode
,pu.name AS projectUnitName

/*
专有属性
*/ 
/*管道分类*/ 
,pl.pipelineClassification AS pipelineClassification
/*管道号*/ 
,pl.pipeNumber 
/*主体直径*/ 
,pl.bodyDiameter 
/*主体壁厚*/ 
,pl.thicknessOfMainBodyWall 
/*主体材质*/ 
,pl.bodyMaterial 
/*设计压力*/ 
,pl.designPressure 
/*设计温度*/ 
,pl.designTemperature
/*操作压力*/ 
,pl.onStreamPressure 
/* 操作温度*/
,pl.operatingTemperature 
/*操作介质*/ 
,pl.operatingMedium
/*试验压力*/ 
,pl.testPressure 
/*试验介质*/ 
,pl.testMedium 
/*试验结果*/ 
,pl.testResult 
/*起始位置*/ 
,pl.zeroPosition 
/*结束位置*/ 
,pl.endPosition 
/* 管道级别*/
,pl.pipelineLevel 
/* 管道等级*/
,pl.pipingClassification 
/* 流程图号*/
,pl.flowChartNumber 
/*施工图号*/ 
,pl.constructionDrawingNumber
/*无损检测比例*/ 
,pl.NDEProportion 
/* 无损检测合格级别*/
,pl.NDEQualificationLevel 
/*无损检查标准*/ 
,pl.NDEStandard 
/*试压包号*/ 
,pl.pressureTestPacketNumber 
/*施工图版本*/ 
,pl.ConstructionDrawingVersion 
/*管外径*/ 
,pl.externalDiameter AS externalDiameter_pl 
/*壁厚等级*/ 
,pl.wallThicknessLevel AS wallThicknessLevel_pl 
/* 是否热处理*/
,pl.heatTreating_pl 


from 

pw_pipeline pl 
/*项目表*/ 
inner join pw_project p on p.id = pl.projectId
/*单项工程表表*/ 
inner join pw_projectitem pi on pi.id = pl.projectItemId
/*单位工程表*/ 
inner join pw_projectunit pu on pu.id = pl.projectUnitId

) pl on w.pipeId = pl.pipelineId
left join pw_weldingposition wp on wp.id =  w.weldingPosition
/*无损检测已经剔除返修*/ 
left join view_nde_mid_horizontal nde on w.id=nde.weldid
/*热处理已经剔除返修*/ 
left join (select ht.* from pw_htentrustmentdailyinfo ht where ht.fx='0' ) ht on ht.weldid = w.id 
order by pl.projectCode asc ,pl.projectItemCode asc ,pl.projectUnitCode asc ,pl.pipeNumber asc ,w.weldNumber asc 

