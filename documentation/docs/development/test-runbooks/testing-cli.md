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
medatarun auth create_user --admin=false --fullName="My User" --password="some.dummy.0000" --username="my.user"
echo $?
```

Should return 1, and you should see

```txt
Remote invocation failed with status 500
Bad credentials.
{"details":"Bad credentials."}
```
