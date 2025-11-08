import {type PropsWithChildren} from 'react'
import './App.css'
import {CommandsPage} from "./components/CommandsPage.tsx";
import {ModelsPage} from "./components/ModelsPage.tsx";
import {ModelPage} from "./components/ModelPage.tsx";

function Layout({children}: PropsWithChildren) {
  return <div>
    <nav className="menubar">
      <div>Medatarun</div>
      <div><a href="/">Home</a></div>
      <div><a href="/">Models</a></div>
      <div><a href="/commands">Commands</a></div>
    </nav>
    <main className="container">{children}</main>
  </div>
}

function App() {
  const path = window.location.pathname
  let content = <div>Not found</div>
  if (path == "/commands") {
    content = <CommandsPage/>
  } else if (path == "/") {
    content = <ModelsPage onClickModel={() => {
  }}/>
  } else if (path.startsWith("/model")) {
    const modelId = path.split("/")[1]
    content = <ModelPage modelId={modelId} />
  }
  return (
    <Layout>{content}</Layout>
  )
}

export default App
