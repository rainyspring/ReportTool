/**
 * sql拼写过程中不能使用单行注视（2个中划线+一个空格 ）
 * 报表--无损检测
 * */
select 
/*日委托明细id*/ 
i.id AS infoId
/*日委托单头id*/ 
,h.id AS headId
/*委托单编号*/ 
,h.commissionCode 
/* 委托日期*/
,h.commissionDate 
/* 委托单位*/
,h.commissionUnit
/*委托单类型 */ 
,h.entrustmentType
/* 检测方法*/
,i.ndeTypeId 
/*接收单位*/ 
,i.receivedUnit 
/*检测标准*/ 
,i.ndeStandard
/* 合格级别*/
,i.qualificationLevel 
/*报告编号*/ 
,i.reportCode as ndeReportCode
/*检测日期*/ 
,i.ndeDate 
/*检测结果*/ 
,i.ndeResult 

/*片子总数*/ 
,i.picNumber
/*不合格片子数*/ 
,i.noNumber 
/*备注*/ 
,i.note as ndeNote
/*是否允许层间检测*/ 
,i.isNDEInterlayer
/*检测长度*/ 
,i.ndeLength
/*检测时机*/ 
,i.ndeOpportunity
/*技术等级*/ 
,i.technicalLevel
/*是否为返修*/ 
,i.fx
/*返修次数*/ 
,i.fxTimes 
/*返修父亲*/ 
,i.fx_parentId 

/*是否为扩探*/ 
,i.kt
/*扩探次数*/ 
,i.ktTimes 
/*扩探父亲*/ 
,i.kt_parentId 
/*委托中的焊缝号，如果是返修焊口，焊口号=原始焊口号+R+返修次数*/ 
,(case when i.fx='1' then  concat(w.weldNumber,'R', i.fxTimes) else w.weldNumber  end) as weldNumber_actual

/*#######焊缝#########*/ 
,w.id AS weldId
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
,w.externalDiameter 
/*设计压力*/ 
,w.designPressure_wl 
/*坡口形式*/ 
,w.grooveType 
/* 坡口形式*/
,w.ddWeldingDate
/* 壁厚等级*/
,w.wallThicknessLevel_wl 

/*#######管道#########*/ 
,pl.id AS pipelineId
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
,pl.NDEStandard as NDEStandard_pl


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
/*质检日委托明细*/ 
pw_ndeentrustmentdailyinfo i
/*质检委托单头*/ 
inner join pw_ndeentrustmentdaily h on h.id = i.headId

inner join pw_weld w on w.id = i.weldId
left join pw_weldingposition wp on wp.id =  w.weldingPosition
inner join pw_pipeline pl on pl.id = w.pipeId
INNER JOIN pw_project p ON w.projectId=p.id
INNER JOIN pw_projectitem pi ON w.projectItemId=pi.id
INNER JOIN pw_projectunit pu ON w.projectUnitId=pu.id
order by pl.pipeNumber asc ,w.weldNumber asc 
