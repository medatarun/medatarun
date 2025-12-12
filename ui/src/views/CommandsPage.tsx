import {Fragment, useEffect, useMemo, useState} from "react";
import {ActionRegistry, executeAction, fetchActionDescriptors} from "../business/command.tsx";


function OutputDisplay({output}: { output: unknown }) {
  if (output === null || output === undefined) {
    return String(output);
  }

  if (typeof output === "string") {
    return output;
  }

  try {
    return JSON.stringify(output, null, 2);
  } catch {
    return String(output);
  }
}

export function CommandsPage() {
  const [commandRegistryDto, setCommandRegistryDto] = useState<ActionRegistry | undefined>(undefined)
  useEffect(() => {
    fetchActionDescriptors()
      .then(data => setCommandRegistryDto(new ActionRegistry(data)))
      .catch(err => console.log(err))
  }, [])
  if (!commandRegistryDto) return null
  return <CommandsPageLoaded actionRegistry={commandRegistryDto}/>
}

export function CommandsPageLoaded({actionRegistry}: { actionRegistry: ActionRegistry }) {
  const defaultResource = actionRegistry.findFirstResourceName()
  const defaultAction = defaultResource ? actionRegistry.findFirstActionName(defaultResource) : undefined
  const defaultPayload = actionRegistry.createPayloadTemplate(defaultResource, defaultAction)

  const [selectedResource, setSelectedResource] = useState<string | undefined>(defaultResource)
  const [selectedAction, setSelectedAction] = useState<string | undefined>(defaultAction)
  const [payload, setPayload] = useState<string>(defaultPayload)
  const [output, setOutput] = useState<unknown>({})
  const [errorMessage, setErrorMessage] = useState<string>("")


  const resourceNames = actionRegistry.resourceNames

  const actionsForSelectedResource = useMemo(() => {
    return actionRegistry.findActionDtoListByResource(selectedResource)
  }, [selectedResource, actionRegistry])

  const selectedActionDescriptor = useMemo(() => {
    return actionRegistry.findActionDto(selectedResource, selectedAction)
  }, [selectedAction, selectedResource, actionRegistry])

  const handleSubmit = () => {
    if (!selectedResource || !selectedAction) {
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


    executeAction(selectedResource, selectedAction, parsedPayload)
      .then(data => setOutput(data))
      .catch(err => setOutput({error: err.toString()}));
  }
  const handleClear = () => {
    setOutput({})
  }

  const handleChangeResource = (resource: string) => {
    const nextResource = actionRegistry.existsResource(resource) ? resource : undefined
    const nextAction = nextResource ? actionRegistry.findFirstActionName(nextResource) : undefined
    setSelectedResource(nextResource)
    setSelectedAction(nextAction)
    setPayload(actionRegistry.createPayloadTemplate(nextResource, nextAction))
  }

  const handleChangeAction = (action: string) => {
    if (!selectedResource) {
      setSelectedAction(undefined)
      setPayload("{}")
    } else {
      const nextAction = actionRegistry.existsAction(selectedResource, action) ? action : undefined
      const nextPayload = actionRegistry.createPayloadTemplate(selectedResource, nextAction)
      setSelectedAction(nextAction)
      setPayload(nextPayload)
    }
  }

  return <div>
    <h1>Commands</h1>
    <div>
      <div>
        <div>
        </div>
        <div style={{display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "1em"}}>
          <div>
            <div style={{display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "1em"}}>
              <div>
                <label>
                  <select value={selectedResource} onChange={(e) => handleChangeResource(e.target.value)}>
                    {resourceNames.map(resource => <option key={resource} value={resource}>{resource}</option>)}
                  </select>
                </label>
              </div>
              <div>
                <label>
                  <select
                    value={selectedAction}
                    onChange={(e) => handleChangeAction(e.target.value)}
                    disabled={!selectedResource || actionsForSelectedResource.length === 0}>
                    {actionsForSelectedResource.map(action => <option key={action.name}
                                                                      value={action.name}>{action.name}</option>)}
                  </select>
                </label>
              </div>

            </div>
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
        <pre style={{border: "1px solid green", padding: "1em"}}>
          <OutputDisplay output={output}/>

      </pre>
      </div>
    </div>
  </div>
}

