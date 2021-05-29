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

order by p.code asc ,pi.code asc ,pu.code asc ,pl.pipeNumber asc
