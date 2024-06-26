# Based on the course [Rock the JVM](https://rockthejvm.com/) course [ZIO Rite of Passage](https://rockthejvm.com/courses/enrolled/2132116)

For mac M1 users using [Colima project](https://github.com/abiosoft/colima) with test containers
```bash
brew install --HEAD colima docker docker-compose
sudo ln -s $HOME/.colima/default/docker.sock /var/run/docker.sock
colima start --cpu 2 --memory 8 --disk 60 --arch aarch64 --vm-type=vz --vz-rosetta --mount-type virtiofs --network-address

export TESTCONTAINERS_HOST_OVERRIDE=$(colima ls -j | jq -r '.address' | head -n 1)
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock
```

For running testcontainers locally you need file `.env`
```bash
# For Colima and TestContainers working on MacOS
DOCKER_HOST=unix://$HOME/.colima/default/docker.sock
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
TESTCONTAINERS_HOST_OVERRIDE=192.168.106.4
```

sbt: ~fastOptJS
npm run start
docker exec -it zio-rite-of-passage-db-1 psql -U docker
\c reviewboard
\d invites



http post localhost:8080/users email='tsg@tsgcompany.com' password='test'
http post localhost:8080/users/login email='tsg@tsgcompany.com' password='test'
http post localhost:8080/companies name='Google' url='google.com' country='USA' location='Boston' industry='tech' tags:='["it","good"]' 'Authorization: Bearer XXX'
http post localhost:8080/companies name='Libertex' url='https://libertex.com/' country='Montenegro' location='Podgorica' industry='fintech' tags:='["it","finance"]' 'Authorization: Bearer XXX'
http post localhost:8080/companies name='Rostec' url='https://rostec.ru/' country='Russia' location=Moscow industry='gos' tags:='["weapons","war"]' 'Authorization: Bearer XXX'
http post localhost:8080/companies/search countries:='["Montenegro"]' locations:='[]' industries:='[]' tags:='[]'

### Add review insert
```sql
insert into reviews(id, company_id, user_id, management, culture, salary, benefits, would_recommend, review, created, updated) values(2, 1, 1, 5, 4, 5, 5, 5, 'Awesome', now(), now());
```

#Add invites insert
```sql
insert into invites(id, user_name, company_id, n_invites, active) values (1, 'tsg@tsgcompany.com',1 ,10, true);

```

### Test invites (just for local testing)
export TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaXNzIjoidHNnY29tcGFueS5jb20iLCJleHAiOjE3MTI1MDcxMjEsImlhdCI6MTcxMjQyMDcyMSwidXNlcm5hbWUiOiJ0c2dAdHNnY29tcGFueS5jb20ifQ.6q_P--EnneNiE-2jPCT52lC766Obwgo-ad1oQd-SD_rjvjZ_Qh_b1cVDcJPR-1Szz_B9rT9Jp1LgfN49DHg8Xw

http post localhost:8080/invite/add companyId=1 "Authorization: Bearer $TOKEN"
select * from invites;

http get localhost:8080/invite/all "Authorization: Bearer $TOKEN"

http post localhost:8080/invite companyId=1 emails:='["mail1@tsgcompany.com", "mail2@tsgcompany.com", "mail3@tsgcompany.com"]' "Authorization: Bearer $TOKEN"

### Test promoted invites
http post localhost:8080/invite/promoted companyId=1 "Authorization: Bearer $TOKEN"

### Get Stripe secret
stripe listen --forward-to http://localhost:8080/invite/webhook
whsec_64c78cf3330cc8cc2d988b00e9858a0b113c7ee5142593bc84865ee6913148c4

### Test reviews summary
http get localhost:8080/reviews/company/1/summary
http post localhost:8080/reviews/company/1/summary

### sbt commands
stagingBuild / Docker / publishLocal

docker save -o server.tar rockthejvm-reviewboard-staging:1.0.1
scp server.tar root@139.59.146.144:/staging
docker load -i server.tar


