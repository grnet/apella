#!/usr/bin/env bash

for i in `seq 1 512`;
do
    echo $i
    #curl -H "X-Auth-Token:5PQFdsz8XsaK9_ihtu2zEw85aeLh9l-H2bASoLnWlETagclRRkoPEKdXlUVpF_pE" http://localhost:8080/dep/rest/statistics &
    curl "http://localhost:8080/dep/rest/exports/candidacy?X-Auth-Token=5PQFdsz8XsaK9_ihtu2zEw85aeLh9l-H2bASoLnWlETagclRRkoPEKdXlUVpF_pE" 1>/dev/null 2>&1 &
done