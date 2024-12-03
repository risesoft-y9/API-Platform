-- 删除表
DROP TABLE y9_interface.view_approve_list;
--DROP TABLE IF EXISTS view_approve_list;

-- 创建视图
create
or replace
algorithm = UNDEFINED view `y9_interface`.`view_approve_list` as
select `nia`.`ID`                   as `APPROVE_ID`,
       `nia`.`INTERFACE_ID`         as `INTERFACE_ID`,
       `nia`.`APPLY_ID`             as `APPLY_ID`,
       `nia`.`APPROVE_STATUS`       as `APPROVE_STATUS`,
       `nia`.`ILLUSTRATE`           as `ILLUSTRATE`,
       `nia`.`APPLY_TYPE`           as `APPLY_TYPE`,
       `nia`.`NOTES`                as `NOTES`,
       `nia`.`IS_OVER`              as `IS_OVER`,
       `nia`.`INTERFACE_STATUS`     as `APPROVE_INTERFACE_STATUS`,
       `nia`.`PERSON_ID`            as `APPROVE_PERSON_ID`,
       `nia`.`PERSON_NAME`          as `APPROVE_PERSON_NAME`,
       `nia`.`CREATETIME`           as `CREATETIME`,
       `nia`.`UPDATETIME`           as `UPDATETIME`,
       `nia`.`CURRENT_USER_ID`      as `CURRENT_USER_ID`,
       `nia`.`ALREADY_APPROVE_USER` as `ALREADY_APPROVE_USER`,
       `nia2`.`APPLY_PERSON_ID`     as `APPLY_PERSON_ID`,
       `nia2`.`APPLY_PERSON_NAME`   as `APPLY_PERSON_NAME`,
       `nia2`.`CREATETIME`          as `apply_time`,
       `nimi`.`INTERFACE_NAME`      as `INTERFACE_NAME`,
       `nimi`.`INTERFACE_STATUS`    as `INTERFACE_STATUS`,
       `nimi`.`VERSION`             as `VERSION`,
       `nimi`.`IS_LIMIT_DATA`       as `IS_LIMIT_DATA`,
       (case
            when (`nia`.`APPROVE_STATUS` = '未审批') then '0'
            when (`nia`.`APPROVE_STATUS` = '审批中') then '1'
            when (`nia`.`APPROVE_STATUS` = '通过') then '2'
            when (`nia`.`APPROVE_STATUS` = '不通过') then '3'
           end)                     as `STATUS_SORT`
from ((`y9_interface`.`y9_interface_approve` `nia`
    left join `y9_interface`.`y9_interface_apply` `nia2` on
    ((`nia`.`APPLY_ID` = `nia2`.`ID`)))
    left join `y9_interface`.`y9_interface_manage_info` `nimi` on
    ((`nia`.`INTERFACE_ID` = `nimi`.`ID`)))
order by `nia`.`UPDATETIME` desc;
