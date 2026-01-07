# Runbook - Testing the CLI

## Goal

To be able to test CLI use-cases. This runbook exists because writing an end-to-end test
architecture at this stage of the project is too heavy. 

## Server mode

### Server logs

```bash
./medatarun serve
```

in another terminal,

```bash
medatarun help model 
```

Test: 
- that server launches, you can see logs
- that command gets you help on model actions
- UI is ok on http://localhost:8080

### Server port 

Goal: test that server works on the right port and that CLI can connect to it.

```bash
./medatarun serve -Dmedatarun.server.port=8081
```

in another terminal, 

 ```bash
medatarun -Dmedatarun.server.port=8081 help model 
```

Test: 
- that server launches, 
- that command gets you help on model actions
- that UI is ok on http://localhost:8081


### Server host

Goal: test that server works on the right port/host and remotely, and that CLI can connect to it.

```bash
./medatarun serve -Dmedatarun.server.host=192.168.0.101 -Dmedatarun.server.port=8081
```
in another terminal,

 ```bash
medatarun -Dmedatarun.server.host=192.168.0.101 -Dmedatarun.server.port=8081 help model 
```

Test:
- that server launches,
- that command gets you help on model actions
- that UI is ok on http://192.168.0.101:8081

### Default logging configuration

Goal: test that errors from CLI are in stderr and not stdio. That server logs are on stdin for INFO and higher levels.

```bash
./medatarun serve
```

in another terminal,

 ```bash
medatarun config Inspect2 2>err.txt 1>ok.txt 
```

Test:
- that you see INFO logs in server console but not DEBUG logs
- that command gives you an error in red in stderr
- `cat err.txt` there should be an error
- `cat ok.txt` there should be instructions

### Exit with the right OS code

Goal: test that scripts can stop if there are errors

```bash
medatarun config Inspect
echo $?
```

Should return 0

Without authentication, do that:
```bash
medatarun auth create_user --admin=false --fullname="My User" --password="some.dummy.0000" --username="my.user"
echo $?
```

Should return 1, and you should see

```txt
Remote invocation failed with status 500
Bad credentials.
{"details":"Bad credentials."}
```

## Testing authentication

Make sure you don't have any environment variable with a token and try to create user

```bash
export MEDATARUN_AUTH_TOKEN=
medatarun auth create_user --admin=false --fullname="My User" --password="some.dummy.0000" --username="my.user"
```
Test:
- that `echo $?` returns `500`
- that you have this message: `ERROR CLI - 500 - Bad credentials. {"details":"Bad credentials."}`

Now log in as admin and put the token in an environment variable `MEDATARUN_AUTH_TOKEN`. Then try to create user again. 

```bash
export MEDATARUN_AUTH_TOKEN=$(curl -X POST --location "http://localhost:8080/auth/login" -H "Content-Type: application/json" -d '{ "username": "youradmin", "password": "yourpassword" }' | jq -r '.access_token')
medatarun auth create_user --admin=false --fullname="My User" --password="some.dummy.0000" --username="my.user"
```

Test:
- You should see : `{"status":"ok"}`
- and `echo $?` should return `0` (process exited with success)

Now you should be able to login with this user

```bash
export MEDATARUN_AUTH_TOKEN=$(curl -X POST --location "http://localhost:8080/auth/login" -H "Content-Type: application/json" -d '{ "username": "my.user", "password": "some.dummy.0000" }' | jq -r '.access_token')
curl "http://localhost:8080/api/me" -H "Authorization: Bearer $MEDATARUN_AUTH_TOKEN" -H "Content-Type: application/json"
```