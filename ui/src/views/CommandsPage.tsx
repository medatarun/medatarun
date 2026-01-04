import {Fragment, useMemo, useState} from "react";
import {ActionRegistry, type ActionResp, executeAction, useActionRegistry} from "../business";
import {ActionOutput} from "../components/business/ActionOutput.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {Field, Textarea} from "@fluentui/react-components";
import {Button, InputCombobox} from "@seij/common-ui";


export function CommandsPage() {
  const commandRegistryDto = useActionRegistry()
  return <CommandsPageLoaded actionRegistry={commandRegistryDto}/>
}

export function CommandsPageLoaded({actionRegistry}: { actionRegistry: ActionRegistry }) {
  const defaultGroupKey = actionRegistry.findFirstGroupKey()
  const defaultActionKey = defaultGroupKey ? actionRegistry.findFirstActionKey(defaultGroupKey) : undefined
  const defaultPayload = actionRegistry.createPayloadTemplate(defaultGroupKey, defaultActionKey)

  const [selectedGroupSearch, setSelectedGroupSearch] = useState<string>(defaultGroupKey ?? "")
  const [selectedGroupKey, setSelectedGroupKey] = useState<string | undefined>(defaultGroupKey)

  const [selectedActionSearch, setSelectedActionSearch] = useState<string>(defaultActionKey ?? "")
  const [selectedActionKey, setSelectedActionKey] = useState<string | undefined>(defaultActionKey)

  const [payload, setPayload] = useState<string>(defaultPayload)
  const [output, setOutput] = useState<ActionResp | null>(null)
  const [errorMessage, setErrorMessage] = useState<string>("")


  const actionGroupKeys = actionRegistry.actionGroupKeys

  const actionsInGroup = useMemo(() => {
    return actionRegistry.findActionDtoListByResource(selectedGroupKey)
  }, [selectedGroupKey, actionRegistry])

  const selectedActionDescriptor = useMemo(() => {
    return actionRegistry.findAction(selectedGroupKey, selectedActionKey)
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
      .catch(err => setOutput({contentType: "json", json: {error: err.toString()}}));
  }
  const handleClear = () => {
    setOutput({contentType: "text", text: ""})
  }

  const handleChangeActionGroup = (groupKey: string | undefined) => {
    const nextGroup = groupKey == undefined ? undefined : actionRegistry.existsGroup(groupKey) ? groupKey : undefined
    const nextAction = nextGroup ? actionRegistry.findFirstActionKey(nextGroup) : undefined
    setSelectedGroupKey(nextGroup)
    setSelectedGroupSearch(nextGroup ?? "")
    setSelectedActionKey(nextAction)
    setSelectedActionSearch(nextAction ?? "")
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
      setSelectedActionSearch(nextAction ?? "")
      setPayload(nextPayload)
    }
  }

  const actionGroupOptions = actionGroupKeys
    .map(it => ({code: it, label: it}))
    .sort((a, b) => a.label.localeCompare(b.label))

  const actionOptions = useMemo(() => actionsInGroup
      .map(it => ({code: it.key, label: it.key}))
      .sort((a, b) => a.label.localeCompare(b.label)),
    [actionsInGroup])


  return <ViewLayoutContained title={"Command panel"}>
    <ViewTitle eyebrow="Command panel">Run commands</ViewTitle>
    <div>
      <div>
        <div>
        </div>
        <div style={{display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "1em"}}>
          <div>
            <div style={{display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "1em"}}>
              <Field label="Group">
                <InputCombobox
                  placeholder="Select a action group"
                  disabled={actionGroupKeys.length === 0}
                  onValueChangeQuery={setSelectedGroupSearch}
                  searchQuery={selectedGroupSearch}
                  onValueChange={handleChangeActionGroup}
                  options={actionGroupOptions}/>
              </Field>
              <Field label="Action">
                <InputCombobox
                  placeholder="Select an action"
                  disabled={!selectedGroupKey || actionsInGroup.length === 0}
                  onValueChangeQuery={setSelectedActionSearch}
                  searchQuery={selectedActionSearch}
                  onValueChange={handleChangeAction}
                  options={actionOptions}
                />
              </Field>
            </div>
            {selectedActionDescriptor ?
                <div style={{padding: "1em"}}>
                <div style={{marginBottom: "0.5em"}}>{selectedActionDescriptor.description}</div>
                <div style={{display: "grid", gridTemplateColumns: "auto auto", columnGap: "1em", rowGap: "0.5em"}}>
                  {selectedActionDescriptor.parameters.map(parameter => <Fragment key={parameter.name}>
                    <div>{parameter.name}</div>
                    <div>{parameter.type} {parameter.optional ? "?" : ""}</div>
                  </Fragment>)}
                </div>
                </div>
              :
              <div>Aucune action sélectionnée</div>}
          </div>
          <div>
            <Field label="Payload">
              <Textarea placeholder="Enter a payload" value={payload} onChange={(e) => setPayload(e.target.value)}
                        rows={6}/>
            </Field>

            <div>
              <Button variant="primary" onClick={handleSubmit}>Submit</Button>
              <Button variant="secondary" onClick={handleClear}>Clear</Button>
            </div>
            {errorMessage ? <div style={{color: "red"}}>{errorMessage}</div> : ""}
          </div>
        </div>
      </div>
      <div>
        {output &&
          <pre style={{border: "1px solid green", padding: "1em"}}>
          <ActionOutput resp={output}/>
          </pre>
        }
      </div>
    </div>
  </ViewLayoutContained>
}

