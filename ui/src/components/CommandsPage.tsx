import {useEffect, useState} from "react";

interface CommandRegistryDto extends Record<string, ActionDescriptorDto[]> {
}

interface ActionDescriptorDto {
  name: string,
  title: string | null,
  description: string | null,
  parameters: ActionParamDescriptorDto[]
}

interface ActionParamDescriptorDto {
  name: string
  type: string
  optional: boolean
}

export function CommandsPage() {
  const defaultCommandStr = JSON.stringify({action: "model/Import", payload: {"from": ""}}, null, 2)
  const [command, setCommand] = useState<string>(defaultCommandStr)
  const [commandRegistryDto, setCommandRegistryDto] = useState<CommandRegistryDto | undefined>(undefined)
  const [output, setOutput] = useState<unknown>({})
  useEffect(() => {
    fetch("/api")
      .then(res => res.json())
      .then(data => setCommandRegistryDto(data))
      .catch(err => console.log(err))
  }, [])
  const handleSubmit = () => {
    const cmd = JSON.parse(command)
    fetch("/api/" + cmd.action, {method: "POST", body: JSON.stringify(cmd.payload)})
      .then(res => res.json())
      .then(data => setOutput(data))
  }
  const handleClear = () => {
    setOutput({})
  }
  const handleRunTemplate = (t: any) => {
    setCommand(JSON.stringify(t, null, 2))
  }
  return <div>
    <h1>Commands</h1>
    <div>

      <div>
        <textarea value={command} onChange={(e) => setCommand(e.target.value)} rows={6}/>
        <div>
          <button type="button" onClick={handleSubmit}>Submit</button>
          <a href="#" onClick={handleClear}>Clear</a>
        </div>
      </div>
      <div>
        {Object.getOwnPropertyNames(output).length === 0 ? "" :
          <pre style={{border: "1px solid green", padding: "1em"}}>{JSON.stringify(output, null, 2)}</pre>}
        {commandRegistryDto ? <CommandRegistryView
            registry={commandRegistryDto}
            onRunTemplate={handleRunTemplate}/> :
          <span>Loading...</span>}
      </div>
    </div>
  </div>
}

function CommandRegistryView({registry, onRunTemplate}: {
  registry: CommandRegistryDto,
  onRunTemplate: (template: any) => void
}) {
  return <div>
    {Object.entries(registry).map(e => {
      const [resource, actions] = e;
      return <div>
        <h2>{resource}</h2>
        <div style={{display: "grid", gridTemplateColumns: "auto auto", columnGap: "1em"}}>
          {actions.map(action =>
            <ActionDescriptionView key={action.name} resource={resource} action={action} onRun={onRunTemplate}/>
          )}
        </div>
      </div>;
    })}
  </div>
}

function ActionDescriptionView({resource, action, onRun}: {
  resource: string,
  action: ActionDescriptorDto,
  onRun: (template: any) => void
}) {
  const handleClick = () => {
    const payload: Record<string, string> = {}
    action.parameters.forEach(param => {
      payload[param.name] = ""
    })
    onRun({action: resource + "/" + action.name, payload: payload});
  }
  return <>
    <div onClick={handleClick}><a href="#">▶️{action.name}</a></div>
    <div style={{
      marginBottom: "1em",
      gridTemplateColumns: "auto auto",
      columnGap: "1em",
      rowGap: "1em"
    }}>
      <div>{action.description}</div>
      <div style={{
        marginLeft: "2em",
        display: "grid",
        gridTemplateColumns: "auto auto",
        columnGap: "1em"
      }}>{action.parameters.map(p => <>
        <div>{p.name}</div>
        <div>{p.type} {p.optional ? "?" : ""}</div>
      </>)}</div>
    </div>
  </>
}
