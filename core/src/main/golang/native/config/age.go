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

func GenX25519KeyPair() (secretKey string, publicKey string, err error) {
	return age.GenX25519KeyPair()
}

func GenHybridKeyPair() (secretKey string, publicKey string, err error) {
	return age.GenHybridKeyPair()
}

func ToPublicKeys(secretKeys ...string) (publicKeys []string, err error) {
	return age.ToPublicKeys(secretKeys...)
}

func VerifySecretKeys(secretKeys ...string) error {
	return age.VeritySecretKeys(secretKeys...)
}

func VerifyPublicKeys(publicKeys ...string) error {
	return age.VerityPublicKeys(publicKeys...)
}
