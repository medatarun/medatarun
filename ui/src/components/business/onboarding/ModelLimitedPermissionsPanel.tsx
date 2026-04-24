import { useCurrentActor } from "@/components/business/auth-actor";
import { useSecurityContext } from "@/components/business/security";
import { Link } from "@tanstack/react-router";

export function ModelLimitedPermissionsPanel() {
  const actor = useCurrentActor();
  const sec = useSecurityContext();
  const isAdmin = actor.isAdmin();
  const cannotRead = !sec.canExecuteAction("model_list");
  if (!cannotRead) return null 
  return (
    <div>
      <p>
        <strong>Get fully ready</strong>
      </p>
      <div>
        <p>On this page are usually displayed the models of Medatarun.</p>
        <p>But, your current permissions are too low too see that.</p>
        <p>This is a bit sad because it is the main intent of Medatarun.</p>
        {isAdmin && (
          <div>
            <strong>Since you are administrator: </strong>
            <ul>
              <li>
                <Link to={"/admin/users"}>Create your first user.</Link>
              </li>
              <li>
                Then, assign him one of our predefined role, or a role you
                create yourself.
              </li>
            </ul>
          </div>
        )}
        {!isAdmin && (
          <p>You should ask your administrator for more permissions.</p>
        )}
      </div>
    </div>
  );
}