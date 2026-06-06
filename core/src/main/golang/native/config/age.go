package config

import (
	"strings"

	"github.com/metacubex/mihomo/component/age"
)

func SetGlobalSecretKeys(secretKeys ...string) {
	keys := make([]string, 0, len(secretKeys))
	for _, k := range secretKeys {
		k = strings.TrimSpace(k)
		if k != "" {
			keys = append(keys, k)
		}
	}
	age.SetGlobalSecretKeys(keys...)
}
