# ag-Grid Server-Side Oracle Example

A reference implementation showing how to perform server-side operations using Oracle with ag-Grid.

![](https://github.com/ag-grid/ag-grid-docs/blob/latest/src/oracle-server-side-operations/oracle-enterprise.png "")

For full details see: http://ag-grid.com/oracle-server-side-operations/

## Usage

- Clone the project
- run `mvn clean install`
- start with `mvn spring-boot:run`
- open browser at `localhost:9090`


## setting up the oracle database for this application

load oracle locally via docker.. and not run on redundant 8080 port
see https://seamaszhou.github.io/2019/03/12/Docker/
```bash
⏣ docker pull store/oracle/database-enterprise:12.2.0.1
⏣ docker run -d -p 8081:8080 -p 1521:1521 --name OracleDB store/oracle/database-enterprise:12.2.0.1
⏣ docker exec -it OracleDB bash -c "source /home/oracle/.bashrc; sqlplus /nolog"
⏣ sleep 60 <- wait for container to start without errors .. especially out of disk errors .. that cause weird library problems.. recommend docker image prune .. docker volumne prune
```

```sql
conn sys as sysdba; -- Now enter the password as 'Oradoc_db1'
alter session set "_ORACLE_SCRIPT"=true;
create user DBadmin identified by DBadmin;
GRANT CONNECT, RESOURCE, DBA TO DBadmin;
CREATE TABLE trade
(
    product VARCHAR(255), portfolio VARCHAR(255), book VARCHAR(255), tradeId NUMBER, submitterId NUMBER, submitterDealId NUMBER, dealType VARCHAR(255), bidType VARCHAR(255),
    currentValue NUMBER, previousValue NUMBER, pl1 NUMBER, pl2 NUMBER, gainDx NUMBER, sxPx NUMBER, x99Out NUMBER, batch NUMBER
);
```

load the data.. wait for awhile
```bash
⏣ mvn -Dtest=TradeDataLoader test
```
