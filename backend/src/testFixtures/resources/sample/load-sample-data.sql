\set ON_ERROR_STOP on

\if :{?confirm}
\else
    \set confirm ''
\endif

select :'confirm' = current_database() as confirmed \gset

\if :confirmed
    begin;
    \ir sample-data.sql
    commit;
    \echo 'Sample data loaded into' :DBNAME
\else
    \echo 'Refusing to load sample data into' :DBNAME
    \echo 'Re-run with: -v confirm=':DBNAME
    \quit
\endif


