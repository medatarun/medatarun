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
  const defaultGroupKey = actionRegistry.findFirstGroupKey()
  const defaultActionKey = defaultGroupKey ? actionRegistry.findFirstActionKey(defaultGroupKey) : undefined
  const defaultPayload = actionRegistry.createPayloadTemplate(defaultGroupKey, defaultActionKey)

  const [selectedGroupKey, setSelectedGroupKey] = useState<string | undefined>(defaultGroupKey)
  const [selectedActionKey, setSelectedActionKey] = useState<string | undefined>(defaultActionKey)
  const [payload, setPayload] = useState<string>(defaultPayload)
  const [output, setOutput] = useState<unknown>({})
  const [errorMessage, setErrorMessage] = useState<string>("")


  const actionGroupKeys = actionRegistry.actionGroupKeys

  const actionsInGroup = useMemo(() => {
    return actionRegistry.findActionDtoListByResource(selectedGroupKey)
  }, [selectedGroupKey, actionRegistry])

  const selectedActionDescriptor = useMemo(() => {
    return actionRegistry.findActionDto(selectedGroupKey, selectedActionKey)
  }, [selectedActionKey, selectedGroupKey, actionRegistry])

  const handleSubmit = () => {
    if (!selectedGroupKey || !selectedActionKey) {
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


    executeAction(selectedGroupKey, selectedActionKey, parsedPayload)
      .then(data => setOutput(data))
      .catch(err => setOutput({error: err.toString()}));
  }
  const handleClear = () => {
    setOutput({})
  }

  const handleChangeActionGroup = (groupKey: string) => {
    const nextGroup = actionRegistry.existsGroup(groupKey) ? groupKey : undefined
    const nextAction = nextGroup ? actionRegistry.findFirstActionKey(nextGroup) : undefined
    setSelectedGroupKey(nextGroup)
    setSelectedActionKey(nextAction)
    setPayload(actionRegistry.createPayloadTemplate(nextGroup, nextAction))
  }

  const handleChangeAction = (action: string) => {
    if (!selectedGroupKey) {
      setSelectedActionKey(undefined)
      setPayload("{}")
    } else {
      const nextAction = actionRegistry.existsAction(selectedGroupKey, action) ? action : undefined
      const nextPayload = actionRegistry.createPayloadTemplate(selectedGroupKey, nextAction)
      setSelectedActionKey(nextAction)
      setPayload(nextPayload)
    }
  }

  return <div>
    <h1>Actions</h1>
    <div>
      <div>
        <div>
        </div>
        <div style={{display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "1em"}}>
          <div>
            <div style={{display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "1em"}}>
              <div>
                <label>
                  <select value={selectedGroupKey} onChange={(e) => handleChangeActionGroup(e.target.value)}>
                    {actionGroupKeys.map(resource => <option key={resource} value={resource}>{resource}</option>)}
                  </select>
                </label>
              </div>
              <div>
                <label>
                  <select
                    value={selectedActionKey}
                    onChange={(e) => handleChangeAction(e.target.value)}
                    disabled={!selectedGroupKey || actionsInGroup.length === 0}>
                    {actionsInGroup.map(action => <option key={action.name}
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

