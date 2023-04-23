export default {
  en: {
    message: {
      common: {
        "yes": "Yes",
        "no": "No",
        "cancel": "Cancel",
        "create": "Create",
        "delete": "Delete",
        "name": "Name",
        "application": "Application",
        "environment": "Environment",
        "created-at": "Created at",
        "search": "Search",
        "create-fail": "Creation failed.",
      },
      customer: {
        "title": "Workspace",
        "delete-title": "Are you sure to delete {0}?",
        "delete-success": "Workspace deleted.",
        "delete-fail": "Deletion failed.",
        "no-result": "No search results found.",
        "empty": "No workspace is registered.",
        "empty-help": "Please create a workspace.",
        "customer-create": "Create workspace",
        "create-success": "Created.",
        "duplicated": "Duplicated workspace name.",
      },
      navigator: {
        "dashboard": "Dashboard",
        "snapshot": "Snapshots ({0}/{1})",
        "manage": "Management"
      },
      dashboard: {
        summary: {
          "method-count": "Total count of methods",
          "agent-count": "Count of agents",
          "application-count": "Count of applications",
          "environment-count": "Count of environments",
          "configuration": "Configuration file"
        },
        configuration: {
          "create": "Show Scavenger configuration file generator.",
          "title": "Create scavenger configuration file",
          "packages": "packages",
          "codebase": "codeBase",
          "annotations": "annotations",
          "method-visibility": "method visibility",
          "exclude-constructors": "exclude constructors",
          "exclude-constructors-checkbox": "Check when excluding constructors from code scanning",
          "exclude-getter-setter": "exclude Getters / Setters",
          "exclude-getter-setter-checkbox": "Check when excluding Getters / Setters from code scanning",
          "environment": "environment (recommended)",
          "app-version": "application version",
          "exclude-packages": "exclude packages",
          "async-codebase-scan-mode": "async code base scan mode",
          // eslint-disable-next-line max-len
          "async-codebase-scan-mode-checkbox": "After confirming that the scavenger is working well, check this to resolve the boot delay by code scanning",
          "download": "Download",
        },
        agent: {
          "title": "Agents",
          "host": "Host name",
          "version": "Version",
          "last-polled-at": "Last polled at",
          "next-poll-expected-at": "Next poll expected at",
        },
      },
      snapshot: {
        "title": "Snapshots",
        "create": "Create",
        "packages": "Packages",
        "filter-invoked-at-millis": "Filter invoked at",
        "edit": "Edit",
        "refresh": "Refresh",
        "refresh-success": "Snapshot regeneration succeeded.",
        "delete-title": "Are you sure to delete the snapshot?",
        "delete-success": "Snapshot deleted.",
        "empty-agent": "Before creating a snapshot, please start Scavenger agent.",
        "limit": "Maximum number of snapshot creation reached. Please delete the existing snapshot.",
        "export": "Export",
        "export-fail": "Failed to export.",
        form: {
          "create-title": "Create Snapshot",
          "edit-title": "Edit Snapshot ({0})",
          "filter-invoked-at-millis-description": "filter invoked after",
          "validation": "The name, application, and environment fields are mandatory.",
          "update-start": "Updating the snapshot.",
          "update-success": "Snapshot is updated successfully.",
          "update-fail": "Snapshot update is failed.",
          "create-start": "Creating the snapshot.",
          "create-success": "Snapshot is created successfully.",
          "limit": "The maximum number of snapshots has been exceeded.",
        },
        detail: {
          "on-refreshing": "The snapshot is already being refreshed.",
          "refreshing": "Refreshing snapshot.",
          "refresh-success": "Snapshot is refreshed successfully.",
          "search": "Type more than 5 characters.",
          // eslint-disable-next-line max-len
          "open-idea-fail": "Need to install IntelliJ IDEA <a href='https://plugins.jetbrains.com/plugin/19991-ide-remote-control' target='_blank' style='text-decoration: none'>IDE Remote Control</a> plug-in.",
          result: {
            "signature": "Signature ({0})",
            "last-invoked-at-millis": "Last invoked at",
            "method-count": "Count of methods",
            "usage": "Usage"
          }
        }
      },
      manage: {
        "reset": "Reset",
        "jvm-count": "Count of JVMs",
        "snapshot-count": "Count of snapshots",
        "invocation-count": "Count of invocations",
        github: {
          "title": "Github mappings",
          "package": "Package",
          "delete-title": "Are you sure to delete the Github mapping?",
          "delete-success": "Github mapping deleted.",
          "create": "Create",
          "create-title": "Create Github mapping",
          "validation": "The package, URL fields are mandatory.",
          "creating": "Start creating Github mapping.",
          "create-success": "Github mapping is created successfully.",
          "duplicated": "Duplicated package.",
        },
        environment: {
          "title": "Environments",
          // eslint-disable-next-line max-len
          "delete-title": "Are you sure to delete the environment({0})? All associated data, such as jvm, snapshots, and invocations will be deleted.",
          "delete-success": "Environment deleted."
        },
        application: {
          "title": "Applications",
          // eslint-disable-next-line max-len
          "delete-title": "Are you sure to delete the application({0})? All associated data, such as jvm, snapshots, and invocations will be deleted.",
          "delete-success": "Application deleted."
        }
      }
    }
  },
  ko: {
    message: {
      common: {
        "yes": "예",
        "no": "아니오",
        "cancel": "취소",
        "create": "생성",
        "delete": "삭제",
        "name": "이름",
        "application": "애플리케이션",
        "environment": "환경",
        "created-at": "생성일",
        "search": "검색",
        "create-fail": "생성에 실패했습니다.",
      },
      customer: {
        "title": "워크스페이스",
        "delete-title": "{0}를 삭제하시겠습니까?",
        "delete-success": "삭제했습니다.",
        "delete-fail": "삭제에 실패했습니다.",
        "no-result": "검색 결과가 없습니다.",
        "empty": "등록된 워크스페이스가 없습니다.",
        "empty-help": "워크스페이스를 생성해 주세요.",
        "customer-create": "워크스페이스 생성",
        "create-success": "생성되었습니다.",
        "duplicated": "워크스페이스 이름이 중복됩니다.",
      },
      navigator: {
        "dashboard": "대시보드",
        "snapshot": "스냅샷 ({0}/{1})",
        "manage": "관리"
      },
      dashboard: {
        summary: {
          "method-count": "전체 메서드 수",
          "agent-count": "에이전트 수",
          "application-count": "애플리케이션 수",
          "environment-count": "환경 수",
          "configuration": "설정 파일"
        },
        configuration: {
          "create": "Scavenger 설정 파일 생성하기",
          "title": "Scavenger 설정 파일 생성",
          "packages": "수집 대상 packages",
          "codebase": "codeBase 위치",
          "annotations": "수집 대상 클래스 annotation",
          "method-visibility": "수집 대상 method visibility",
          "exclude-constructors": "수집 대상 생성자 제외여부",
          "exclude-constructors-checkbox": "생성자를 수집 제외시 체크",
          "exclude-getter-setter": "수집 대상 Getter / Setter 제외 여부",
          "exclude-getter-setter-checkbox": "Getter / Setter를 수집 제외 시 체크",
          "environment": "environment 구분자 (권장)",
          "app-version": "version 구분자",
          "exclude-packages": "수집 제외 package",
          "async-codebase-scan-mode": "codeBase 수집 async 모드 활성화",
          "async-codebase-scan-mode-checkbox": "Scavenger가 정상 동작한 것을 확인한 뒤에, scavenger에 의한 부팅 지연을 해소하고자 한다면 체크",
          "download": "다운로드",
        },
        agent: {
          "title": "에이전트 목록",
          "host": "호스트명",
          "version": "버전",
          "last-polled-at": "최근 데이터 수집",
          "next-poll-expected-at": "다음 데이터 수집",
        },
      },
      snapshot: {
        "title": "스냅샷 목록",
        "create": "생성하기",
        "packages": "포함 패키지",
        "filter-invoked-at-millis": "처리 기간",
        "edit": "변경",
        "refresh": "재생성",
        "refresh-success": "스냅샷 재생성 완료.",
        "delete-title": "스냅샷을 삭제하시겠습니까?",
        "delete-success": "스냅샷을 삭제했습니다.",
        "empty-agent": "스냅샷을 생성하기 전에, 수집을 시작해 주세요.",
        "limit": "최대 스냅샷 생성 개수에 도달하였습니다. 기존 스냅샷을 삭제해 주세요.",
        "export": "내보내기",
        "export-fail": "내보내기에 실패했습니다.",
        form: {
          "create-title": "스냅샷 생성",
          "edit-title": "스냅샷 변경 ({0})",
          "filter-invoked-at-millis-description": "이후 호출 건만 처리",
          "validation": "이름, 애플리케이션, 환경은 필수로 입력되어야 합니다.",
          "update-start": "스냅샷 변경을 시작합니다.",
          "update-success": "스냅샷 변경 성공.",
          "update-fail": "변경에 실패했습니다.",
          "create-start": "스냅샷 생성을 시작합니다.",
          "create-success": "스냅샷 생성 성공.",
          "limit": "스냅샷 최대 개수를 초과하였습니다.",
        },
        detail: {
          "on-refreshing": "이미 스냅샷을 재생성하는 중 입니다.",
          "refreshing": "스냅샷을 재생성하는 중 입니다.",
          "refresh-success": "스냅샷 재생성 완료.",
          "search": "검색하려면 5자 이상을 입력하세요.",
          // eslint-disable-next-line max-len
          "open-idea-fail": "IntelliJ IDEA의 <a href='https://plugins.jetbrains.com/plugin/19991-ide-remote-control' target='_blank' style='text-decoration: none'>IDE Remote Control</a> 플러그인 설치가 필요합니다.</span>",
          result: {
            "signature": "시그니쳐 ({0})",
            "last-invoked-at-millis": "마지막 호출 시간",
            "method-count": "메서드 수",
            "usage": "사용률"
          }
        }
      },
      manage: {
        "reset": "전체 초기화",
        "jvm-count": "JVM 수",
        "snapshot-count": "스냅샷 수",
        "invocation-count": "기록된 메서드 수",
        github: {
          "title": "Github 매핑 목록",
          "package": "패키지",
          "delete-title": "Github 매핑을 삭제하시겠습니까?",
          "delete-success": "매핑을 삭제하였습니다.",
          "create": "신규",
          "create-title": "Github 맵핑 생성",
          "validation": "패키지, URL은 필수로 입력되어야 합니다.",
          "creating": "매핑 생성을 시작합니다.",
          "create-success": "매핑 생성 성공.",
          "duplicated": "패키지가 중복됩니다.",
        },
        environment: {
          "title": "환경 목록",
          "delete-title": "환경({0})을 삭제하시겠습니까? jvm, 스냅샷, 호출 기록 등 연관된 모든 데이터가 삭제됩니다.",
          "delete-success": "환경을 삭제하였습니다."
        },
        application: {
          "title": "애플리케이션 목록",
          "delete-title": "애플리케이션({0})을 삭제하시겠습니까? jvm, 스냅샷, 호출 기록 등 연관된 모든 데이터가 삭제됩니다.",
          "delete-success": "애플리케이션을 삭제하였습니다."
        }
      }
    }
  }
}
