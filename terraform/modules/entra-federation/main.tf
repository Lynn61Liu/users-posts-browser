locals {
  name_prefix        = "${var.project_name}-${var.environment}"
  create_federation  = trimspace(var.entra_metadata_file) != ""
  saml_provider_name = var.saml_provider_name != "" ? var.saml_provider_name : "${local.name_prefix}-entra-idp"

  role_names = {
    devops_engineer  = "${local.name_prefix}-DevOpsEngineer"
    readonly_auditor = "${local.name_prefix}-ReadOnlyAuditor"
  }

  project_role_arns = [
    "arn:${data.aws_partition.current.partition}:iam::${data.aws_caller_identity.current.account_id}:role/${local.name_prefix}-ecs-task-execution-role",
    "arn:${data.aws_partition.current.partition}:iam::${data.aws_caller_identity.current.account_id}:role/${local.name_prefix}-frontend-task-role",
    "arn:${data.aws_partition.current.partition}:iam::${data.aws_caller_identity.current.account_id}:role/${local.name_prefix}-backend-task-role",
    "arn:${data.aws_partition.current.partition}:iam::${data.aws_caller_identity.current.account_id}:role/${local.name_prefix}-codebuild-role",
    "arn:${data.aws_partition.current.partition}:iam::${data.aws_caller_identity.current.account_id}:role/${local.name_prefix}-codedeploy-role",
    "arn:${data.aws_partition.current.partition}:iam::${data.aws_caller_identity.current.account_id}:role/${local.name_prefix}-codepipeline-role"
  ]
}

data "aws_partition" "current" {}

data "aws_caller_identity" "current" {}

resource "aws_iam_saml_provider" "entra" {
  count = local.create_federation ? 1 : 0

  name                   = local.saml_provider_name
  saml_metadata_document = file(var.entra_metadata_file)
  tags                   = var.common_tags
}

data "aws_iam_policy_document" "saml_assume_role" {
  count = local.create_federation ? 1 : 0

  statement {
    sid     = "AllowAssumeRoleWithAzureEntraSAML"
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithSAML"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_saml_provider.entra[0].arn]
    }

    condition {
      test     = "StringEquals"
      variable = "SAML:aud"
      values   = ["https://signin.aws.amazon.com/saml"]
    }
  }
}

resource "aws_iam_role" "devops_engineer" {
  count = local.create_federation ? 1 : 0

  name                 = local.role_names.devops_engineer
  assume_role_policy   = data.aws_iam_policy_document.saml_assume_role[0].json
  max_session_duration = var.max_session_duration
  tags                 = var.common_tags
}

resource "aws_iam_role" "readonly_auditor" {
  count = local.create_federation ? 1 : 0

  name                 = local.role_names.readonly_auditor
  assume_role_policy   = data.aws_iam_policy_document.saml_assume_role[0].json
  max_session_duration = var.max_session_duration
  tags                 = var.common_tags
}

resource "aws_iam_role_policy_attachment" "devops_power_user" {
  count = local.create_federation ? 1 : 0

  role       = aws_iam_role.devops_engineer[0].name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/PowerUserAccess"
}

resource "aws_iam_role_policy_attachment" "devops_iam_read_only" {
  count = local.create_federation ? 1 : 0

  role       = aws_iam_role.devops_engineer[0].name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/IAMReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "readonly_auditor_read_only" {
  count = local.create_federation ? 1 : 0

  role       = aws_iam_role.readonly_auditor[0].name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/ReadOnlyAccess"
}

data "aws_iam_policy_document" "devops_pass_project_roles" {
  count = local.create_federation ? 1 : 0

  statement {
    sid       = "PassProjectServiceRoles"
    effect    = "Allow"
    actions   = ["iam:PassRole"]
    resources = local.project_role_arns

    condition {
      test     = "StringLike"
      variable = "iam:PassedToService"
      values = [
        "ecs-tasks.amazonaws.com",
        "codebuild.amazonaws.com",
        "codedeploy.amazonaws.com",
        "codepipeline.amazonaws.com"
      ]
    }
  }
}

resource "aws_iam_role_policy" "devops_pass_project_roles" {
  count = local.create_federation ? 1 : 0

  name   = "${local.name_prefix}-devops-pass-project-roles"
  role   = aws_iam_role.devops_engineer[0].id
  policy = data.aws_iam_policy_document.devops_pass_project_roles[0].json
}
