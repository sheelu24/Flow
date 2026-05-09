Flow – Workflow Builder  
1. Project Overview 
Build a workflow automation platform where users can: 
● Create workflows in Draft 
● Add trigger + action nodes 
● Connect nodes in order 
● Add if/else branching 
● Publish workflows 
● Run workflows using: 
○ Webhook 
○ Schedule (cron) 
○ Manual Run 
● View run history, logs, and failures 
2. Major Functional Requirements 
Workflow Management 
● Create workflow 
● Edit workflow in Draft 
● Publish workflow 
● Pause / Resume workflow 
● Delete workflow 
● View workflow details 
● View execution history 
Triggers 
● Webhook Trigger 
● Schedule Trigger (cron) 
● Manual Trigger (Run Now button) 
Node Types 
● HTTP Request Node → call external API (GET/POST) 
● Condition Node → true/false branching 
● Delay Node → wait for N seconds 
● Notify Node → send Slack or Email 
Execution 
● Run nodes in sequence 
● Support condition branching 
● Save workflow run status 
● Save node-level logs 
● Retry failed steps 
● Show failure reason 
Monitoring 
● Show all runs 
● Show success/failure status 
● Show logs 
● Show dashboard status 
Authentication 
● User login/register 
● JWT authentication 
● Basic multi-user support 
3. Major Non-Functional Requirements 
● Scalable → handle multiple workflow runs 
● Reliable → retry failed steps 
● Extensible → easy to add new node types later 
● Secure → JWT auth + webhook secret 
● Idempotent → avoid duplicate webhook runs 
● Observable → logs, run history, health checks 
● Maintainable → clean modular code 
4. Main UI Screens 
● Login / Register 
● Dashboard 
● Workflow List 
● Workflow Builder Page 
● Workflow Details 
● Run History 
● Run Details / Logs 
7. Main APIs 
Auth APIs 
●  Register - POST /api/auth/register 
●  Login - POST /api/auth/login 
Workflow APIs 
●  Create Workflow - POST /api/workflows 
●  Get All Workflows - GET /api/workflows 
●  Get Workflow By Id - GET /api/workflows/{id} 
●  Update Workflow - PUT /api/workflows/{id} 
●  Delete Workflow - DELETE /api/workflows/{id} 
●  Publish Workflow - POST /api/workflows/{id}/publish 
●  Pause Workflow - POST /api/workflows/{id}/pause 
●  Resume Workflow - POST /api/workflows/{id}/resume 
Node APIs 
●  Get Workflow Graph - GET /api/workflows/{id}/graph 
●  Add Node - POST /api/workflows/{id}/nodes 
●  Update Node - PUT /api/workflows/{id}/nodes/{nodeId} 
●  Delete Node - DELETE /api/workflows/{id}/nodes/{nodeId} 
Edge APIs 
●  Add Edge - POST /api/workflows/{id}/edges 
●  Delete Edge - DELETE /api/workflows/{id}/edges/{edgeId} 
Trigger APIs 
●  Create Webhook Trigger - POST /api/workflows/{id}/triggers/webhook 
●  Create Schedule Trigger - POST /api/workflows/{id}/triggers/schedule 
Execution APIs 
●  Run Workflow Manually - POST /api/workflows/{id}/run 
● Webhook Trigger API - POST /api/hooks/{webhookPath} 
● Get Workflow Runs - GET /api/workflows/{id}/runs 
● Get Run Details - GET /api/runs/{runId} 
● Get Node Logs - GET /api/runs/{runId}/logs 
● Dashboard API - GET /api/monitoring/dashboard 
Database Schema  
1. users 
● id 
● name 
● email 
● password_hash 
● role 
● created_at 
2. workflows 
● id 
● user_id 
● name 
● description 
● status (DRAFT, PUBLISHED, PAUSED) 
● created_at 
● updated_at 
3. workflow_nodes 
● id 
● workflow_id 
● name 
● node_type (WEBHOOK_TRIGGER, SCHEDULE_TRIGGER, HTTP_REQUEST, 
CONDITION, DELAY, NOTIFY) 
● config_json 
● position_x 
● position_y 
● is_start_node 
4.workflow_edges 
● id 
● workflow_id 
● source_node_id 
● target_node_id 
● edge_type (DEFAULT, TRUE, FALSE) 
5. workflow_triggers 
● id 
● workflow_id 
● trigger_type (WEBHOOK, SCHEDULE) 
● webhook_path 
● webhook_secret 
● cron_expression 
● is_enabled 
6. workflow_runs 
● id 
● workflow_id 
● trigger_type 
● status (QUEUED, RUNNING, SUCCESS, FAILED) 
● input_payload 
● output_payload 
● started_at 
● ended_at 
● error_message 
7.node_runs 
● id 
● workflow_run_id 
● node_id 
● status 
● input_payload 
● output_payload 
● started_at 
● ended_at 
● error_message 
8.execution_logs 
● id 
● workflow_run_id 
● node_run_id 
● log_level 
● message 
● created_at
