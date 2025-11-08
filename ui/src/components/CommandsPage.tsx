import {Fragment, useEffect, useMemo, useState} from "react";

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
  const [commandRegistryDto, setCommandRegistryDto] = useState<CommandRegistryDto | undefined>(undefined)
  const [selectedResource, setSelectedResource] = useState<string>("")
  const [selectedAction, setSelectedAction] = useState<string>("")
  const [payload, setPayload] = useState<string>("{}")
  const [output, setOutput] = useState<unknown>({})
  const [errorMessage, setErrorMessage] = useState<string>("")
  useEffect(() => {
    fetch("/api")
      .then(res => res.json())
      .then(data => setCommandRegistryDto(data))
      .catch(err => console.log(err))
  }, [])
  const resourceNames = useMemo(() => commandRegistryDto ? Object.keys(commandRegistryDto) : [], [commandRegistryDto])
  const actionsForSelectedResource = useMemo(() => {
    if (!selectedResource || !commandRegistryDto) {
      return []
    }
    return commandRegistryDto[selectedResource] ?? []
  }, [selectedResource, commandRegistryDto])
  const selectedActionDescriptor = useMemo(() => {
    return actionsForSelectedResource.find(action => action.name === selectedAction)
  }, [actionsForSelectedResource, selectedAction])

  const applySelection = (resource: string, actionName?: string) => {
    if (!commandRegistryDto) {
      setSelectedResource(resource)
      setSelectedAction(actionName ?? "")
      setPayload("{}")
      return
    }
    const availableActions = commandRegistryDto[resource] ?? []
    const resolvedAction = actionName && availableActions.find(a => a.name === actionName) ? actionName :
      (availableActions[0]?.name ?? "")
    const descriptor = availableActions.find(a => a.name === resolvedAction)
    setSelectedResource(resource)
    setSelectedAction(resolvedAction)
    setPayload(descriptor ? buildPayloadTemplate(descriptor) : "{}")
  }

  useEffect(() => {
    if (!commandRegistryDto || resourceNames.length === 0) {
      return
    }
    if (selectedResource) {
      const actions = commandRegistryDto[selectedResource] ?? []
      if (actions.length > 0 && !selectedAction) {
        applySelection(selectedResource, actions[0].name)
      }
      return
    }
    const defaultResource = resourceNames[0]
    const defaultAction = commandRegistryDto[defaultResource]?.[0]?.name
    applySelection(defaultResource, defaultAction)
  }, [commandRegistryDto, resourceNames, selectedResource, selectedAction])

  const handleSubmit = () => {
    if (!selectedResource || !selectedActionDescriptor) {
      setErrorMessage("Sélect a resource and an action.")
      return
    }
    let parsedPayload
    try {
      parsedPayload = JSON.parse(payload)
    } catch (e) {
      setErrorMessage("Invalid payload: " + (e instanceof Error ? e.message : "unknown error"))
      return
    }
    setErrorMessage("")
    const actionPath = selectedActionDescriptor.name.includes("/") ?
      selectedActionDescriptor.name :
      selectedResource + "/" + selectedActionDescriptor.name
    fetch("/api/" + actionPath, {method: "POST", body: JSON.stringify(parsedPayload)})
      .then(res => res.json())
      .then(data => setOutput(data))
  }
  const handleClear = () => {
    setOutput({})
  }
  const handleRunTemplate = (resource: string, action: ActionDescriptorDto) => {
    applySelection(resource, action.name)
  }
  return <div>
    <h1>Commands</h1>
    <div>
      <div>
        <div>
          <label>Resource&nbsp;
            <select value={selectedResource} onChange={(e) => applySelection(e.target.value)}>
              {resourceNames.map(resource => <option key={resource} value={resource}>{resource}</option>)}
            </select>
          </label>
        </div>
        <div>
          <label>Action&nbsp;
            <select
              value={selectedAction}
              onChange={(e) => applySelection(selectedResource, e.target.value)}
              disabled={!selectedResource || actionsForSelectedResource.length === 0}>
              {actionsForSelectedResource.map(action => <option key={action.name}
                                                                value={action.name}>{action.name}</option>)}
            </select>
          </label>
        </div>
        <div style={{display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "1em"}}>
          <div style={{border: "1px solid #ddd", padding: "1em"}}>
            <h3>Documentation</h3>
            {selectedActionDescriptor ?
              <>
                <div style={{marginBottom: "0.5em"}}>{selectedActionDescriptor.description}</div>
                <div style={{display: "grid", gridTemplateColumns: "auto auto", columnGap: "1em", rowGap: "0.5em"}}>
                  {selectedActionDescriptor.parameters.map(parameter => <Fragment key={parameter.name}>
                    <div>{parameter.name}</div>
                    <div>{parameter.type} {parameter.optional ? "?" : ""}</div>
                  </Fragment>)}
                </div>
              </> :
              <div>Aucune action sélectionnée</div>}
          </div>
          <div>
            <textarea value={payload} onChange={(e) => setPayload(e.target.value)} rows={6}/>
            <div>
              <button type="button" onClick={handleSubmit}>Submit</button>
              <a href="#" onClick={handleClear}>Clear</a>
            </div>
            {errorMessage ? <div style={{color: "red"}}>{errorMessage}</div> : ""}
          </div>
        </div>
      </div>
      <div>
        {Object.getOwnPropertyNames(output).length === 0 ? "" :
          <pre style={{border: "1px solid green", padding: "1em"}}>{JSON.stringify(output, null, 2)}</pre>}
        {commandRegistryDto ? <CommandRegistryView
            registry={commandRegistryDto}
            onSelectTemplate={handleRunTemplate}/> :
          <span>Loading...</span>}
      </div>
    </div>
  </div>
}

function CommandRegistryView({registry, onSelectTemplate}: {
  registry: CommandRegistryDto,
  onSelectTemplate: (resource: string, action: ActionDescriptorDto) => void
}) {
  return <div>
    {Object.entries(registry).map(e => {
      const [resource, actions] = e;
      return <div>
        <h2>{resource}</h2>
        <div style={{display: "grid", gridTemplateColumns: "auto auto", columnGap: "1em"}}>
          {actions.map(action =>
            <ActionDescriptionView key={action.name} resource={resource} action={action} onRun={onSelectTemplate}/>
          )}
        </div>
      </div>;
    })}
  </div>
}

function ActionDescriptionView({resource, action, onRun}: {
  resource: string,
  action: ActionDescriptorDto,
  onRun: (resource: string, action: ActionDescriptorDto) => void
}) {
  const handleClick = () => {
    onRun(resource, action);
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

function buildPayloadTemplate(action: ActionDescriptorDto): string {
  const payload: Record<string, string> = {}
  action.parameters.forEach(param => {
    payload[param.name] = ""
  })
  return JSON.stringify(payload, null, 2)
}
